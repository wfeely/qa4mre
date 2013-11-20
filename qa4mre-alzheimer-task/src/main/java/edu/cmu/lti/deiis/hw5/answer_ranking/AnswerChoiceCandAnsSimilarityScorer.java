/*
 * AnswerChoiceCandAnsSimilarityScorer evaluates the similarity between choices of answers and 
 * the candidate sentences by computing the overlapping of noun phrases and NEs
 */
package edu.cmu.lti.deiis.hw5.answer_ranking;

import java.util.ArrayList;

import org.apache.uima.UimaContext;
import org.apache.uima.analysis_component.JCasAnnotator_ImplBase;
import org.apache.uima.analysis_engine.AnalysisEngineProcessException;
import org.apache.uima.jcas.JCas;
import org.apache.uima.jcas.cas.FSList;
import org.apache.uima.resource.ResourceInitializationException;

import edu.cmu.lti.qalab.types.Answer;
import edu.cmu.lti.qalab.types.CandidateAnswer;
import edu.cmu.lti.qalab.types.CandidateSentence;
import edu.cmu.lti.qalab.types.NER;
import edu.cmu.lti.qalab.types.NounPhrase;
import edu.cmu.lti.qalab.types.Question;
import edu.cmu.lti.qalab.types.QuestionAnswerSet;
import edu.cmu.lti.qalab.types.TestDocument;
import edu.cmu.lti.qalab.types.VerbPhrase;
import edu.cmu.lti.qalab.utils.Utils;

public class AnswerChoiceCandAnsSimilarityScorer extends JCasAnnotator_ImplBase {

  int K_CANDIDATES = 5;

  @Override
  public void initialize(UimaContext context) throws ResourceInitializationException {
    super.initialize(context);
    K_CANDIDATES = (Integer) context.getConfigParameterValue("K_CANDIDATES");
  }

  @Override
  public void process(JCas aJCas) throws AnalysisEngineProcessException {
    TestDocument testDoc = Utils.getTestDocumentFromCAS(aJCas);
    // String testDocId = testDoc.getId();
    ArrayList<QuestionAnswerSet> qaSet = Utils.getQuestionAnswerSetFromTestDocCAS(aJCas);

    for (int i = 0; i < qaSet.size(); i++) {

      Question question = qaSet.get(i).getQuestion();
      System.out.println("Question: " + question.getText());
      ArrayList<Answer> choiceList = Utils.fromFSListToCollection(qaSet.get(i).getAnswerList(),
              Answer.class);
      ArrayList<CandidateSentence> candSentList = Utils.fromFSListToCollection(qaSet.get(i)
              .getCandidateSentenceList(), CandidateSentence.class);

      int topK = Math.min(K_CANDIDATES, candSentList.size());
      for (int c = 0; c < topK; c++) {

        CandidateSentence candSent = candSentList.get(c);

        ArrayList<NounPhrase> candSentNouns = Utils.fromFSListToCollection(candSent.getSentence()
                .getPhraseList(), NounPhrase.class);
        ArrayList<NER> candSentNers = Utils.fromFSListToCollection(candSent.getSentence()
                .getNerList(), NER.class);

        ArrayList<VerbPhrase> candSentVerbs = Utils.fromFSListToCollection(candSent.getSentence()
                .getVerbPhraseList(), VerbPhrase.class);

        ArrayList<CandidateAnswer> candAnsList = new ArrayList<CandidateAnswer>();
        for (int j = 0; j < choiceList.size(); j++) {

          Answer answer = choiceList.get(j);
          ArrayList<NounPhrase> choiceNouns = Utils.fromFSListToCollection(
                  answer.getNounPhraseList(), NounPhrase.class);
          ArrayList<NER> choiceNERs = Utils.fromFSListToCollection(answer.getNerList(), NER.class);
          ArrayList<VerbPhrase> choiceVerbs = Utils.fromFSListToCollection(
                  answer.getVerbPhraseList(), VerbPhrase.class);
          int nnMatch = 0;
          for (int k = 0; k < candSentNouns.size(); k++) {
            /* noun phrases from candidate contain NEs from choices */
            for (int l = 0; l < choiceNERs.size(); l++) {
              if (candSentNouns.get(k).getText().contains(choiceNERs.get(l).getText())) {
                nnMatch++;
              }
            }
            /* noun phrases from candidate contain noun phrases from choices */
            for (int l = 0; l < choiceNouns.size(); l++) {
              if (candSentNouns.get(k).getText().contains(choiceNouns.get(l).getText())) {
                nnMatch++;
              }
            }
          }

          for (int k = 0; k < candSentNers.size(); k++) {
            /* NEs from candidate contain noun phrases from choices */
            for (int l = 0; l < choiceNERs.size(); l++) {
              if (candSentNers.get(k).getText().contains(choiceNERs.get(l).getText())) {
                nnMatch++;
              }
            }
            /* NEs from candidate contain NEs from choices */
            for (int l = 0; l < choiceNouns.size(); l++) {
              if (candSentNers.get(k).getText().contains(choiceNouns.get(l).getText())) {
                nnMatch++;
              }
            }
          }

          // verb match
//          for (int k = 0; k < candSentVerbs.size(); k++) {
//            for (int l = 0; l < choiceVerbs.size(); l++) {
//              if (candSentVerbs.get(k).getText().contains(choiceVerbs.get(l).getText())) {
//                nnMatch++;
//              }
//            }
//          }

          System.out.println(choiceList.get(j).getText() + "\t" + nnMatch);
          CandidateAnswer candAnswer = null;
          if (candSent.getCandAnswerList() == null) {
            candAnswer = new CandidateAnswer(aJCas);
          } else {
            candAnswer = Utils.fromFSListToCollection(candSent.getCandAnswerList(),
                    CandidateAnswer.class).get(j);// new
            // CandidateAnswer(aJCas);;

          }
          candAnswer.setText(answer.getText());
          candAnswer.setQId(answer.getQuestionId());
          candAnswer.setChoiceIndex(j);
          candAnswer.setSimilarityScore(nnMatch);
          candAnsList.add(candAnswer);
        }

        FSList fsCandAnsList = Utils.fromCollectionToFSList(aJCas, candAnsList);
        candSent.setCandAnswerList(fsCandAnsList);
        candSentList.set(c, candSent);

      }

      System.out.println("================================================");
      FSList fsCandSentList = Utils.fromCollectionToFSList(aJCas, candSentList);
      qaSet.get(i).setCandidateSentenceList(fsCandSentList);

    }
    FSList fsQASet = Utils.fromCollectionToFSList(aJCas, qaSet);
    testDoc.setQaList(fsQASet);

  }
}
