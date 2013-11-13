

/* First created by JCasGen Mon Nov 11 01:09:29 EST 2013 */
package edu.cmu.lti.qalab.types;

import org.apache.uima.jcas.JCas; 
import org.apache.uima.jcas.JCasRegistry;
import org.apache.uima.jcas.cas.TOP_Type;

import org.apache.uima.jcas.tcas.Annotation;


/** Scored Document type. This is used to compute the average c@1 score. This object is created for each document.
 * Updated by JCasGen Mon Nov 11 01:09:33 EST 2013
 * XML source: /usr0/home/kmuruges/Workspace/hw5-team05/qa4mre-base/src/main/resources/TypeSystemDescriptor.xml
 * @generated */
public class DocumentScore extends Annotation {
  /** @generated
   * @ordered 
   */
  @SuppressWarnings ("hiding")
  public final static int typeIndexID = JCasRegistry.register(DocumentScore.class);
  /** @generated
   * @ordered 
   */
  @SuppressWarnings ("hiding")
  public final static int type = typeIndexID;
  /** @generated  */
  @Override
  public              int getTypeIndexID() {return typeIndexID;}
 
  /** Never called.  Disable default constructor
   * @generated */
  protected DocumentScore() {/* intentionally empty block */}
    
  /** Internal - constructor used by generator 
   * @generated */
  public DocumentScore(int addr, TOP_Type type) {
    super(addr, type);
    readObject();
  }
  
  /** @generated */
  public DocumentScore(JCas jcas) {
    super(jcas);
    readObject();   
  } 

  /** @generated */  
  public DocumentScore(JCas jcas, int begin, int end) {
    super(jcas);
    setBegin(begin);
    setEnd(end);
    readObject();
  }   

  /** <!-- begin-user-doc -->
    * Write your own initialization here
    * <!-- end-user-doc -->
  @generated modifiable */
  private void readObject() {/*default - does nothing empty block */}
     
 
    
  //*--------------*
  //* Feature: avgCAt1Score

  /** getter for avgCAt1Score - gets Average C@1 Score. One for each document
   * @generated */
  public double getAvgCAt1Score() {
    if (DocumentScore_Type.featOkTst && ((DocumentScore_Type)jcasType).casFeat_avgCAt1Score == null)
      jcasType.jcas.throwFeatMissing("avgCAt1Score", "edu.cmu.lti.qalab.types.DocumentScore");
    return jcasType.ll_cas.ll_getDoubleValue(addr, ((DocumentScore_Type)jcasType).casFeatCode_avgCAt1Score);}
    
  /** setter for avgCAt1Score - sets Average C@1 Score. One for each document 
   * @generated */
  public void setAvgCAt1Score(double v) {
    if (DocumentScore_Type.featOkTst && ((DocumentScore_Type)jcasType).casFeat_avgCAt1Score == null)
      jcasType.jcas.throwFeatMissing("avgCAt1Score", "edu.cmu.lti.qalab.types.DocumentScore");
    jcasType.ll_cas.ll_setDoubleValue(addr, ((DocumentScore_Type)jcasType).casFeatCode_avgCAt1Score, v);}    
  }

    