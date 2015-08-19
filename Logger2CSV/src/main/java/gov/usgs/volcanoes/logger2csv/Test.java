 package gov.usgs.volcanoes.logger2csv;

import org.omg.CORBA.portable.ResponseHandler;

/**
 * This example demonstrates the use of the {@link ResponseHandler} to simplify
 * the process of processing the HTTP response and releasing associated resources.
 */
public class Test {

	public void httpClient() {
//        CloseableHttpClient httpclient = HttpClients.createDefault();
//        try {
//            HttpGet httpget = new HttpGet("http://localhost/");
//
//            System.out.println("Executing request " + httpget.getRequestLine());
//
//            // Create a custom response handler
//            ResponseHandler responseHandler = new ResponseHandler() {
//
//                public String handleResponse(
//                        final HttpResponse response) throws ClientProtocolException, IOException {
//                    int status = response.getStatusLine().getStatusCode();
//                    if (status >= 200 && status < 300) {
//                        HttpEntity entity = response.getEntity();
//                        return entity != null ? EntityUtils.toString(entity) : null;
//                    } else {
//                        throw new ClientProtocolException("Unexpected response status: " + status);
//                    }
//                }
//
//				public OutputStream createExceptionReply() {
//					// TODO Auto-generated method stub
//					return null;
//				}
//
//				public OutputStream createReply() {
//					// TODO Auto-generated method stub
//					return null;
//				}
//
//            };
//            String responseBody = httpclient.execute(httpget, responseHandler);
//            System.out.println("----------------------------------------");
//            System.out.println(responseBody);
//        } finally {
//            httpclient.close();
//        }
    }

	public static void jsonTest() {
//		JSONObject jo = null;
//		BufferedReader reader = null;
//		try {
//			reader = new BufferedReader(new InputStreamReader(new FileInputStream("test.json")));
//		} catch (FileNotFoundException e1) {
//			// TODO Auto-generated catch block
//			e1.printStackTrace();
//		}
//        StringBuilder builder = new StringBuilder();
//        
//        try {
//			for (String line = null; (line = reader.readLine()) != null;) {
//			    builder.append(line).append("\n");
//			}
//		} catch (IOException e) {
//			// TODO Auto-generated catch block
//			e.printStackTrace();
//		}           
//        JSONTokener tokener = new JSONTokener(builder.toString());
//        try {
//            jo = new JSONObject(tokener);                
//        } catch (JSONException e) {             
////            e.printStackTrace();
////
////            Log.d("parsing", "creating json array");
////            try {
////                rr.jsonArray = new JSONArray(tokener);
////            } catch (JSONException e1) {                    
////                e1.printStackTrace();
////            }
////
//        }
//        
//        System.out.println(": " + jo.get("a"));
	}

	
    public final static void main(String[] args) throws Exception {
    }
}

