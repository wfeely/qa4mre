package edu.cmu.lti.deiis.hw5.answer_selection;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;

import org.apache.uima.UimaContext;
import org.apache.uima.analysis_component.JCasAnnotator_ImplBase;
import org.apache.uima.analysis_engine.AnalysisEngineProcessException;
import org.apache.uima.jcas.JCas;
import org.apache.uima.resource.ResourceInitializationException;

import edu.cmu.lti.qalab.types.Answer;
import edu.cmu.lti.qalab.types.CandidateAnswer;
import edu.cmu.lti.qalab.types.CandidateSentence;
import edu.cmu.lti.qalab.types.Question;
import edu.cmu.lti.qalab.types.QuestionAnswerSet;
import edu.cmu.lti.qalab.types.TestDocument;
import edu.cmu.lti.qalab.utils.Utils;

public class AnswerSelectionByKCandVoting extends JCasAnnotator_ImplBase {

  int K_CANDIDATES = 5;

  public double avgCAt1 = 0.0;

  public int nDoc = 0;

  @Override
  public void initialize(UimaContext context) throws ResourceInitializationException {
    super.initialize(context);
    K_CANDIDATES = (Integer) context.getConfigParameterValue("K_CANDIDATES");
  }

  @Override
  public void process(JCas aJCas) throws AnalysisEngineProcessException {
    TestDocument testDoc = Utils.getTestDocumentFromCAS(aJCas);
    ArrayList<QuestionAnswerSet> qaSet = Utils.fromFSListToCollection(testDoc.getQaList(),
            QuestionAnswerSet.class);
    int matched = 0;
    int total = 0;
    int unanswered = 0;
    int nOATot = 0;
    int nOATP = 0;
    int nOAFP = 0;
    int nOAFN = 0;

    for (int i = 0; i < qaSet.size(); i++) {

      Question question = qaSet.get(i).getQuestion();
      System.out.println("Question: " + question.getText());
      ArrayList<Answer> choiceList = Utils.fromFSListToCollection(qaSet.get(i).getAnswerList(),
              Answer.class);
      ArrayList<CandidateSentence> candSentList = Utils.fromFSListToCollection(qaSet.get(i)
              .getCandidateSentenceList(), CandidateSentence.class);

      int topK = Math.min(K_CANDIDATES, candSentList.size());
      String correct = "";

      for (int j = 0; j < choiceList.size(); j++) {
        Answer answer = choiceList.get(j);
        if (answer.getIsCorrect()) {
          correct = answer.getText();
          break;
        }
      }

      HashMap<String, Double> hshAnswer = new HashMap<String, Double>();

      for (int c = 0; c < topK; c++) {

        CandidateSentence candSent = candSentList.get(c);

        ArrayList<CandidateAnswer> candAnswerList = Utils.fromFSListToCollection(
                candSent.getCandAnswerList(), CandidateAnswer.class);
        String selectedAnswer = "";
        double maxScore = Double.NEGATIVE_INFINITY;
        for (int j = 0; j < candAnswerList.size(); j++) {

          CandidateAnswer candAns = candAnswerList.get(j);
          String answer = candAns.getText();

          double totalScore = candAns.getSimilarityScore() + candAns.getSynonymScore()
                  + candAns.getPMIScore();

          if (totalScore > maxScore) {
            maxScore = totalScore;
            selectedAnswer = answer;
          }
        }
        Double existingVal = hshAnswer.get(selectedAnswer);
        if (existingVal == null) {
          existingVal = new Double(0.0);
        }
        hshAnswer.put(selectedAnswer, existingVal + 1.0);
      }

      String bestChoice = null;
      // Find an answer which is most supported by the Top-k Candidate sentence
      try {
        bestChoice = findBestChoice(hshAnswer);

      } catch (Exception e) {
        e.printStackTrace();
      }
      boolean flag = false;
      for (int j = 0; j < choiceList.size(); j++) {
    	  Answer answer = choiceList.get(j);
    	  if (answer.getText().equals(bestChoice)) {
    		  answer.setIsSelected(true);
    		  flag = true;
    		  break;
    	  }
      }
      if (!flag || bestChoice == null) {
    	  Answer answer = choiceList.get(choiceList.size() - 1);
    	  answer.setIsCorrect(true);
      }
    	  
      System.out.println("Correct Choice: " + "\t" + correct);
      System.out.println("Best Choice: " + "\t" + bestChoice);

      if (bestChoice == null) {
        unanswered++;
        bestChoice="None of the above";
      }
      if (correct == "None of the above") {
        nOATot++;
        if(bestChoice!="None of the above"){
          nOAFN++;
        }
      }else{
        if(bestChoice=="None of the above"){
          nOAFP++;
        }
      }
      System.out.println(">>>>>>>>>>>>>>>>>>>>>>>>>> "+correct+"=?"+bestChoice);
      if (bestChoice != null && correct.equals(bestChoice)) {
        matched++;
        if (correct == "None of the above")
          nOATP++;
      }
      total++;
      System.out.println("================================================");

    }

    System.out.println("Correct: " + matched + "/" + total + "=" + ((matched * 100.0) / total)
            + "%");
    System.out.println(">>>>>>>>>>>>>TP': " + "(" + nOATP + "/" + nOATot + ")");
    System.out.println(">>>>>>>>>>>>>FP': " + "(" + nOAFP + "/" + nOATot + ")");
    System.out.println(">>>>>>>>>>>>>FN': " + "(" + nOAFN + "/" + nOATot + ")");
    // TO DO: Reader of this pipe line should read from xmi generated by
    // SimpleRunCPE
    double cAt1 = (((double) matched) / ((double) total) * unanswered + (double) matched)
            * (1.0 / total);
    System.out.println("c@1 score:" + cAt1);
    avgCAt1 = avgCAt1 + cAt1;
    nDoc++;

  }

  public String findBestChoice(HashMap<String, Double> hshAnswer) throws Exception {

    Iterator<String> it = hshAnswer.keySet().iterator();
    String bestAns = null;
    double maxScore = 0;
    System.out.println("Aggregated counts; ");
    while (it.hasNext()) {
      String key = it.next();
      Double val = hshAnswer.get(key);
      System.out.println(key + "\t" + key + "\t" + val);
      if (val > maxScore) {
        maxScore = val;
        bestAns = key;
      }

    }

    return bestAns;
  }

  @Override
  public void collectionProcessComplete() throws AnalysisEngineProcessException {
    super.collectionProcessComplete();
    System.out.println("\n\n ------------------ RUN RESULT ------------------\n");
    System.out.println("Average C@1: " + ((double) avgCAt1 / nDoc));
  }

}
