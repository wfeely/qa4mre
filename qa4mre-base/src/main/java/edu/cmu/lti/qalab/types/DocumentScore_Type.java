
/* First created by JCasGen Mon Nov 11 01:09:29 EST 2013 */
package edu.cmu.lti.qalab.types;

import org.apache.uima.jcas.JCas;
import org.apache.uima.jcas.JCasRegistry;
import org.apache.uima.cas.impl.CASImpl;
import org.apache.uima.cas.impl.FSGenerator;
import org.apache.uima.cas.FeatureStructure;
import org.apache.uima.cas.impl.TypeImpl;
import org.apache.uima.cas.Type;
import org.apache.uima.cas.impl.FeatureImpl;
import org.apache.uima.cas.Feature;
import org.apache.uima.jcas.tcas.Annotation_Type;

/** Scored Document type. This is used to compute the average c@1 score. This object is created for each document.
 * Updated by JCasGen Mon Nov 11 01:09:33 EST 2013
 * @generated */
public class DocumentScore_Type extends Annotation_Type {
  /** @generated */
  @Override
  protected FSGenerator getFSGenerator() {return fsGenerator;}
  /** @generated */
  private final FSGenerator fsGenerator = 
    new FSGenerator() {
      public FeatureStructure createFS(int addr, CASImpl cas) {
  			 if (DocumentScore_Type.this.useExistingInstance) {
  			   // Return eq fs instance if already created
  		     FeatureStructure fs = DocumentScore_Type.this.jcas.getJfsFromCaddr(addr);
  		     if (null == fs) {
  		       fs = new DocumentScore(addr, DocumentScore_Type.this);
  			   DocumentScore_Type.this.jcas.putJfsFromCaddr(addr, fs);
  			   return fs;
  		     }
  		     return fs;
        } else return new DocumentScore(addr, DocumentScore_Type.this);
  	  }
    };
  /** @generated */
  @SuppressWarnings ("hiding")
  public final static int typeIndexID = DocumentScore.typeIndexID;
  /** @generated 
     @modifiable */
  @SuppressWarnings ("hiding")
  public final static boolean featOkTst = JCasRegistry.getFeatOkTst("edu.cmu.lti.qalab.types.DocumentScore");
 
  /** @generated */
  final Feature casFeat_avgCAt1Score;
  /** @generated */
  final int     casFeatCode_avgCAt1Score;
  /** @generated */ 
  public double getAvgCAt1Score(int addr) {
        if (featOkTst && casFeat_avgCAt1Score == null)
      jcas.throwFeatMissing("avgCAt1Score", "edu.cmu.lti.qalab.types.DocumentScore");
    return ll_cas.ll_getDoubleValue(addr, casFeatCode_avgCAt1Score);
  }
  /** @generated */    
  public void setAvgCAt1Score(int addr, double v) {
        if (featOkTst && casFeat_avgCAt1Score == null)
      jcas.throwFeatMissing("avgCAt1Score", "edu.cmu.lti.qalab.types.DocumentScore");
    ll_cas.ll_setDoubleValue(addr, casFeatCode_avgCAt1Score, v);}
    
  



  /** initialize variables to correspond with Cas Type and Features
	* @generated */
  public DocumentScore_Type(JCas jcas, Type casType) {
    super(jcas, casType);
    casImpl.getFSClassRegistry().addGeneratorForType((TypeImpl)this.casType, getFSGenerator());

 
    casFeat_avgCAt1Score = jcas.getRequiredFeatureDE(casType, "avgCAt1Score", "uima.cas.Double", featOkTst);
    casFeatCode_avgCAt1Score  = (null == casFeat_avgCAt1Score) ? JCas.INVALID_FEATURE_CODE : ((FeatureImpl)casFeat_avgCAt1Score).getCode();

  }
}



    