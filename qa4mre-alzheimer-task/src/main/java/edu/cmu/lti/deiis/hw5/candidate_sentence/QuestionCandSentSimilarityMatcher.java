/*
 * QuestionCandSentSimilarityMatcher finds the candidate sentences of answer which match
 * the question by searching noun phrases and named entities using Solr indexer
 */

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
import edu.cmu.lti.qalab.types.TestDocument;
import edu.cmu.lti.qalab.utils.Utils;

/**
 * This class pick ups candidate sentences that matches the question with high similarity score
 * 
 * @author Siping Ji <sipingji@cmu.edu>
 * 
 */
public class QuestionCandSentSimilarityMatcher extends JCasAnnotator_ImplBase {

  SolrWrapper solrWrapper = null;


  String serverUrl;



  // IndexSchema indexSchema;
  String coreName;

  String schemaName;

  int TOP_SEARCH_RESULTS = 25;

  @Override
  public void initialize(UimaContext context) throws ResourceInitializationException {
    super.initialize(context);
    serverUrl = (String) context.getConfigParameterValue("SOLR_SERVER_URL");
    coreName = (String) context.getConfigParameterValue("SOLR_CORE");
    schemaName = (String) context.getConfigParameterValue("SCHEMA_NAME");
    TOP_SEARCH_RESULTS = (Integer) context.getConfigParameterValue("TOP_SEARCH_RESULTS");
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

    // for each question/answer set, use the question to match sentences
    for (int i = 0; i < qaSet.size(); i++) {

      Question question = qaSet.get(i).getQuestion();
      System.out.println("========================================================");
      System.out.println("Question: " + question.getText());

      // retrieve sentences using some representation of this question (named entities, phrases)
      String searchQuery = this.formSolrQuery(question);
      if (searchQuery.trim().equals("")) {
        continue;
      }
      ArrayList<CandidateSentence> candidateSentList = new ArrayList<CandidateSentence>();
      SolrQuery solrQuery = new SolrQuery();
      solrQuery.add("fq", "docid:" + testDocId); // search sentences only in this test document
      solrQuery.add("q", searchQuery);
      solrQuery.add("rows", String.valueOf(TOP_SEARCH_RESULTS));
      solrQuery.setFields("*", "score");
      try {
        SolrDocumentList results = solrWrapper.runQuery(solrQuery, TOP_SEARCH_RESULTS);
        // for each retrieved sentences
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

          String sentence = doc.get("text").toString();
          // the score is already computed by solr!
          // so we are not going to refine this part, i.e. similarity matching
          // we may instead make effort on forming better query

          double relScore = Double.parseDouble(doc.get("score").toString());
          CandidateSentence candSent = new CandidateSentence(aJCas);
          candSent.setSentence(annSentence);
          candSent.setRelevanceScore(relScore);
          candidateSentList.add(candSent);
          System.out.println(relScore + "\t" + sentence);
        }
        //store candidate sentences
        FSList fsCandidateSentList = Utils.fromCollectionToFSList(aJCas, candidateSentList);
        fsCandidateSentList.addToIndexes();
        // store candidate sentences in question answer set
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
   * queries that retrieves noun-phrases and named entities of a given question
   * 
   * @param question
   * @return
   */
  public String formSolrQuery(Question question) {
    String solrQuery = "";

    ArrayList<NounPhrase> nounPhrases = Utils.fromFSListToCollection(question.getNounList(),
            NounPhrase.class);

    for (int i = 0; i < nounPhrases.size(); i++) {
      solrQuery += "nounphrases:\"" + nounPhrases.get(i).getText() + "\" ";
    }

    ArrayList<NER> neList = Utils.fromFSListToCollection(question.getNerList(), NER.class);
    for (int i = 0; i < neList.size(); i++) {
      solrQuery += "namedentities:\"" + neList.get(i).getText() + "\" ";
    }
    solrQuery = solrQuery.trim();

    return solrQuery;
  }


}
