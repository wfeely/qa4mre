package edu.cmu.lti.deiis.hw5.annotators;

import java.util.ArrayList;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.uima.UimaContext;
import org.apache.uima.analysis_component.JCasAnnotator_ImplBase;
import org.apache.uima.analysis_engine.AnalysisEngineProcessException;
import org.apache.uima.jcas.JCas;
import org.apache.uima.resource.ResourceInitializationException;
import edu.cmu.lti.qalab.types.TestDocument;
import edu.cmu.lti.qalab.utils.Brackets;
import edu.cmu.lti.qalab.utils.Utils;

/**
 * Splits a string with formats such as 201|2Copyright|, |uIntroduction|, etc into two lines. Fix
 * for anomalies like Statistical AnalysisVariation, 2012Copyright, etc..
 * 
 * @author kmuruges
 * 
 */
public class TextSegmenter extends JCasAnnotator_ImplBase {

  private Pattern pattern;

  @Override
  public void initialize(UimaContext context) throws ResourceInitializationException {
    super.initialize(context);
    pattern = Pattern.compile("[0-9a-z][A-Z][a-z/-]+{3}");
  }

  @Override
  public void process(JCas jCas) throws AnalysisEngineProcessException {
    try {
      // Read TestDocuments from the CAS pool. Each TestDocument contain Raw Document Text and
      // QuestionAnswerSet
      TestDocument testDoc = Utils.getTestDocumentFromCAS(jCas);
      System.out.println("###########################" + "Reading Document: " + testDoc.getId()
              + "###########################");
      String docText = testDoc.getText();
      Matcher matcher = pattern.matcher(docText);

      // Get the list of bracketed terms such as (some text/number) ..
      ArrayList<Brackets> brackatedExpression = Utils.findBrackatedExpression(docText);
      ArrayList<Integer> posList = new ArrayList<Integer>();
      while (matcher.find()) {
        int start = matcher.start();
        int end = matcher.end();
        // Check and see whether we can segment the line into two lines
        // Perform a set of rules
        boolean isSegment = true;
        if (Character.isDigit(docText.charAt(start))) {
          if ((start - 1) >= 0 && Character.isDigit(docText.charAt(start - 1))) {
            isSegment = true;
          } else {
            isSegment = false;
          }
        } else {
          if ((start - 1) >= 0 && (docText.charAt(start - 1) == ' ')
                  || docText.charAt(start - 1) == Character.toUpperCase(docText.charAt(start - 1))) {
            isSegment = false;
          }
          if (start - 2 >= 0 && (docText.charAt(start - 2) == ' ')) {
            isSegment = false;
          }
          if (start - 3 >= 0 && (docText.charAt(start - 3) == ' ')) {
            isSegment = false;
          }
        }
        // Check whether the anomaly is in the bracket
        boolean isInsideBkrt = Utils.isInsideBracket(brackatedExpression, start + 1);
        if (isInsideBkrt) {
          isSegment = false;
        }
        if (start + 1 < end && isSegment) {
          posList.add(start + 1);
        }
      }
      // Read each Position and add a new line character. shift variable accounts for adding the new
      // line character.
      int shift = 0;
      for (int i = 0; i < posList.size(); i++) {
        int pos = posList.get(i) + shift;
        String segment1 = docText.substring(0, pos);
        String segment2 = docText.substring(pos);
        docText = segment1 + "\n" + segment2;
        shift++;
      }
      System.out.println(docText);

      testDoc.setText(docText);
      testDoc.addToIndexes();

    } catch (Exception e) {
      e.printStackTrace();
    }
  }

}
