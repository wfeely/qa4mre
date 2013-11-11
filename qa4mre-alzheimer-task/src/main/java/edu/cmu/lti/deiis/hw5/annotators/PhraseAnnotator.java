/**
 * PhraseAnnotator.java
 * Annotates NounPhrases and VerbPhrases
 * @author: Weston Feely
 */
package edu.cmu.lti.deiis.hw5.annotators;

import java.util.ArrayList;

import org.apache.uima.UimaContext;
import org.apache.uima.analysis_component.JCasAnnotator_ImplBase;
import org.apache.uima.analysis_engine.AnalysisEngineProcessException;
import org.apache.uima.jcas.JCas;
import org.apache.uima.jcas.cas.FSList;
import org.apache.uima.resource.ResourceInitializationException;

import edu.cmu.lti.qalab.types.NounPhrase;
import edu.cmu.lti.qalab.types.Sentence;
import edu.cmu.lti.qalab.types.TestDocument;
import edu.cmu.lti.qalab.types.Token;
import edu.cmu.lti.qalab.types.VerbPhrase;
import edu.cmu.lti.qalab.utils.Utils;

public class PhraseAnnotator extends JCasAnnotator_ImplBase {

  @Override
  public void initialize(UimaContext context) throws ResourceInitializationException {
    super.initialize(context);

  }

  @Override
  public void process(JCas aJCas) throws AnalysisEngineProcessException {

    TestDocument testDoc = Utils.getTestDocumentFromCAS(aJCas);

    ArrayList<Sentence> sentenceList = Utils.getSentenceListFromTestDocCAS(aJCas);

    for (int i = 0; i < sentenceList.size(); i++) {

      Sentence sent = sentenceList.get(i);
      ArrayList<Token> tokenList = Utils.getTokenListFromSentenceList(sent);
      // get noun phrases
      ArrayList<NounPhrase> phraseList = extractNounPhrases(tokenList, aJCas);
      FSList fsPhraseList = Utils.createNounPhraseList(aJCas, phraseList);
      fsPhraseList.addToIndexes(aJCas);
      sent.setPhraseList(fsPhraseList);
      // get verb phrases
      ArrayList<VerbPhrase> verbPhraseList = extractVerbPhrases(tokenList, aJCas);
      FSList fsVerbPhraseList = Utils.createVerbPhraseList(aJCas, verbPhraseList);
      fsVerbPhraseList.addToIndexes(aJCas);
      sent.setVerbPhraseList(fsVerbPhraseList);
      // add sentence to indexes
      sent.addToIndexes();
      sentenceList.set(i, sent);
    }

    FSList fsSentList = Utils.createSentenceList(aJCas, sentenceList);
    testDoc.setSentenceList(fsSentList);

  }

  public ArrayList<NounPhrase> extractNounPhrases(ArrayList<Token> tokenList, JCas jCas) {

    ArrayList<NounPhrase> nounPhraseList = new ArrayList<NounPhrase>();
    String nounPhrase = "";
    for (int i = 0; i < tokenList.size(); i++) {
      Token token = tokenList.get(i);
      String word = token.getText();
      String pos = token.getPos();

      // Build a noun phrase by matching POS tags
      // Group5 changes: added determiners, pronouns, possessive pronouns, possessive markers,
      // adverbs to list of allowable NP POS tags
      // TODO: allow prepositions (IN & TO) if at least one noun has already been added
      if (pos.startsWith("NN") || pos.startsWith("JJ") || pos.startsWith("CD")
              || pos.startsWith("DT") || pos.startsWith("PDT") || pos.startsWith("RB")
              || pos.startsWith("PRP") || pos.startsWith("POS")) {
        nounPhrase += word + " ";
      } else {
        nounPhrase = nounPhrase.trim();
        if (!nounPhrase.equals("")) {
          NounPhrase nn = new NounPhrase(jCas);
          nn.setText(nounPhrase);
          nounPhraseList.add(nn);
          // System.out.println("Noun Phrase: "+nounPhrase);
          nounPhrase = "";
        }
      }

    }
    nounPhrase = nounPhrase.trim();
    if (!nounPhrase.equals("")) {
      NounPhrase nn = new NounPhrase(jCas);
      nn.setText(nounPhrase);
      nounPhraseList.add(nn);
    }

    return nounPhraseList;
  }

  public ArrayList<VerbPhrase> extractVerbPhrases(ArrayList<Token> tokenList, JCas jCas) {

    ArrayList<VerbPhrase> verbPhraseList = new ArrayList<VerbPhrase>();
    String verbPhrase = "";
    for (int i = 0; i < tokenList.size(); i++) {
      Token token = tokenList.get(i);
      String word = token.getText();
      String pos = token.getPos();

      // Build a verb phrase by matching POS tags
      // Group5 changes: added verbs, modals, particles, adverbs
      // TODO: allow prepositions (IN & TO) if at least one verb has already been added
      if (pos.startsWith("VB") || pos.startsWith("MD") || pos.startsWith("RP")
              || pos.startsWith("RB")) {
        verbPhrase += word + " ";
      } else {
        verbPhrase = verbPhrase.trim();
        if (!verbPhrase.equals("")) {
          VerbPhrase vv = new VerbPhrase(jCas);
          vv.setText(verbPhrase);
          verbPhraseList.add(vv);
          // System.out.println("VerbPhrase: "+verbPhrase);
          verbPhrase = "";
        }
      }

    }
    verbPhrase = verbPhrase.trim();
    if (!verbPhrase.equals("")) {
      VerbPhrase vv = new VerbPhrase(jCas);
      vv.setText(verbPhrase);
      verbPhraseList.add(vv);
    }

    return verbPhraseList;
  }

}
