package edu.cmu.lti.deiis.hw5.candidate_sentence;

import java.util.ArrayList;

import org.apache.solr.client.solrj.SolrQuery;
import org.apache.solr.client.solrj.SolrServerException;
import org.apache.solr.common.SolrDocument;
import org.apache.solr.common.SolrDocumentList;
import org.apache.uima.UimaContext;
import org.apache.uima.analysis_component.JCasAnnotator_ImplBase;
import org.apache.uima.analysis_engine.AnalysisEngineProcessException;
import org.apache.uima.jcas.JCas;
import org.apache.uima.jcas.cas.FSList;
import org.apache.uima.resource.ResourceInitializationException;

import edu.cmu.lti.oaqa.core.provider.solr.SolrWrapper;
import edu.cmu.lti.qalab.types.CandidateSentence;
import edu.cmu.lti.qalab.types.NER;
import edu.cmu.lti.qalab.types.NounPhrase;
import edu.cmu.lti.qalab.types.Question;
import edu.cmu.lti.qalab.types.QuestionAnswerSet;
import edu.cmu.lti.qalab.types.Sentence;
import edu.cmu.lti.qalab.types.Synonym;
import edu.cmu.lti.qalab.types.TestDocument;
import edu.cmu.lti.qalab.types.VerbPhrase;
import edu.cmu.lti.qalab.utils.Utils;

public class QuestionCandSentSynonymMatcher extends JCasAnnotator_ImplBase {

  SolrWrapper solrWrapper = null;

  String serverUrl;

  // IndexSchema indexSchema;
  String coreName;

  String schemaName;

  boolean addSentWindow = false;

  int TOP_SEARCH_RESULTS = 10;

  @Override
  public void initialize(UimaContext context) throws ResourceInitializationException {
    super.initialize(context);
    serverUrl = (String) context.getConfigParameterValue("SOLR_SERVER_URL");
    coreName = (String) context.getConfigParameterValue("SOLR_CORE");
    schemaName = (String) context.getConfigParameterValue("SCHEMA_NAME");

    try {
      this.solrWrapper = new SolrWrapper(serverUrl + coreName);
    } catch (Exception e) {
      e.printStackTrace();
    }

  }

  @Override
  public void process(JCas aJCas) throws AnalysisEngineProcessException {
    TestDocument testDoc = Utils.getTestDocumentFromCAS(aJCas);
    String testDocId = testDoc.getId();
    ArrayList<Sentence> sentenceList = Utils.getSentenceListFromTestDocCAS(aJCas);
    ArrayList<QuestionAnswerSet> qaSet = Utils.getQuestionAnswerSetFromTestDocCAS(aJCas);

    for (int i = 0; i < qaSet.size(); i++) {

      Question question = qaSet.get(i).getQuestion();
      System.out.println("========================================================");
      System.out.println("Question: " + question.getText());
      String searchQuery = this.formSolrQuery(question);
      if (searchQuery.trim().equals("")) {
        continue;
      }
      ArrayList<CandidateSentence> candidateSentList = new ArrayList<CandidateSentence>();
      SolrQuery solrQuery = new SolrQuery();
      solrQuery.add("fq", "docid:" + testDocId);
      solrQuery.add("q", searchQuery);
      solrQuery.add("rows", String.valueOf(TOP_SEARCH_RESULTS));
      solrQuery.setFields("*", "score");
      try {
        SolrDocumentList results = solrWrapper.runQuery(solrQuery, TOP_SEARCH_RESULTS);
        for (int j = 0; j < results.size(); j++) {
          SolrDocument doc = results.get(j);
          String sentId = doc.get("id").toString();
          String docId = doc.get("docid").toString();
          if (!testDocId.equals(docId)) {
            continue;
          }
          String sentIdx = sentId.replace(docId, "").replace("_", "").trim();
          int idx = Integer.parseInt(sentIdx);
          Sentence annSentence = sentenceList.get(idx);
          // Sentence Window feature of size 3
          if (addSentWindow) {
            Sentence currentSentence = null;
            Sentence prevAnnSentence = null;
            Sentence nextAnnSentence = null;
            // Modify the annotation sentence
            annSentence = null;
            // Retrieve previous sentence
            if ((idx - 1) >= 0) {
              prevAnnSentence = sentenceList.get(idx - 1);
            }
            // Retrieve current sentence
            currentSentence = sentenceList.get(idx);
            // Retrieve previous sentence
            if ((idx + 1) < sentenceList.size()) {
              nextAnnSentence = sentenceList.get(idx + 1);
            }
            // Combine the Sentence Window
            annSentence = (Sentence) currentSentence.clone();
            String text = annSentence.getText();
            FSList nounList = annSentence.getPhraseList();
            FSList nerList = annSentence.getPhraseList();
            ArrayList<NounPhrase> nounArrayList = Utils.fromFSListToCollection(nounList,
                    NounPhrase.class);
            ArrayList<NER> nerArrayList = Utils.fromFSListToCollection(nerList, NER.class);
            if (prevAnnSentence != null) {
              text += prevAnnSentence.getText();
              nounArrayList.addAll(Utils.fromFSListToCollection(prevAnnSentence.getPhraseList(),
                      NounPhrase.class));
              nerArrayList.addAll(Utils.fromFSListToCollection(prevAnnSentence.getNerList(),
                      NER.class));
            }
            if (nextAnnSentence != null) {
              text += nextAnnSentence.getText();
              nounArrayList.addAll(Utils.fromFSListToCollection(nextAnnSentence.getPhraseList(),
                      NounPhrase.class));
              nerArrayList.addAll(Utils.fromFSListToCollection(nextAnnSentence.getNerList(),
                      NER.class));
            }
            // Sentence Window of size 3
            annSentence.setText(text);
            annSentence.setPhraseList(nounList);
            annSentence.setNerList(nerList);
            // annSentence.addToIndexes();
          }
          String sentence = doc.get("text").toString();
          double relScore = Double.parseDouble(doc.get("score").toString());
          CandidateSentence candSent = new CandidateSentence(aJCas);
          candSent.setSentence(annSentence);
          candSent.setSynonymMatchScore(relScore);
          candidateSentList.add(candSent);
          System.out.println(relScore + "\t" + sentence);
        }
        FSList fsCandidateSentList = Utils.fromCollectionToFSList(aJCas, candidateSentList);
        fsCandidateSentList.addToIndexes();
        qaSet.get(i).setCandidateSentenceList(fsCandidateSentList);
        qaSet.get(i).addToIndexes();

      } catch (SolrServerException e) {
        e.printStackTrace();
      }
      FSList fsQASet = Utils.fromCollectionToFSList(aJCas, qaSet);
      testDoc.setQaList(fsQASet);

      System.out.println("=========================================================");
    }

  }

  /**
   * Form query using synonym query expansion
   * 
   * @param question
   * @return
   */
  public String formSolrQuery(Question question) {
    String solrQuery = "";

    ArrayList<NounPhrase> nounPhrases = Utils.fromFSListToCollection(question.getNounList(),
            NounPhrase.class);

    for (int i = 0; i < nounPhrases.size(); i++) {
      NounPhrase np = nounPhrases.get(i);
      System.out.println(np.getSynonyms());
      ArrayList<Synonym> synList = Utils.fromFSListToCollection(np.getSynonyms(), Synonym.class);
      for (int j = 0; j < synList.size(); j++) {
        solrQuery += "synonyms:\"" + synList.get(j).getText() + "\" ";
      }

    }

    ArrayList<NER> neList = Utils.fromFSListToCollection(question.getNerList(), NER.class);
    for (int i = 0; i < neList.size(); i++) {
      NER ner = neList.get(i);
      ArrayList<Synonym> synList = Utils.fromFSListToCollection(ner.getSynonyms(), Synonym.class);
      for (int j = 0; j < synList.size(); j++) {
        solrQuery += "synonyms:\"" + synList.get(j).getText() + "\" ";
      }
    }
    ArrayList<VerbPhrase> verbPhrases = Utils.fromFSListToCollection(question.getVerbPhraseList(),
            VerbPhrase.class);

    for (int i = 0; i < verbPhrases.size(); i++) {
      solrQuery += "verbphrases:\"" + verbPhrases.get(i).getText() + "\" ";
    }

    solrQuery = solrQuery.trim();

    return solrQuery;
  }

}
