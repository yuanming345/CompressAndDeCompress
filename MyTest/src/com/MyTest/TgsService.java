package com.MyTest;
/**
* @interfaceName: TRService 
* @Description: 过车数据上传
* @author zhangb 
* @date 2014-6-17 上午11:06:00 
* @version V1.0
*/
public interface TgsService {

    /**
	 * @Title:passCarInfoUpload
	 * @Description:通过xml上传过车信息
	 * @param xml
	 * @return
	 */
	public String passCarInfoUpload(String xml);
    /**
	 * @Title:passCarInfoUpload
	 * @Description:通过xml上传过车信息
	 * @param xml
	 * @param zjwj1
	 * @param zjwj2
	 * @param zjwj3
	 * @return
	 */
	public String passCarInfoUploadWithImage(String xml,byte[]zjwj1,byte[]zjwj2,byte[]zjwj3,byte[] tztp1,byte[] tztp2,byte[] tztp3,byte[] tztp4);		
	
	/**
	 * @Title:passCarInfoUploadWithSecondRecognition
	 * @Description: 通过xml上传二次识别信息
	 * @param xml
	 * @return
	 */
	public String passCarInfoUploadWithSecondRecognition(String xml);
}
