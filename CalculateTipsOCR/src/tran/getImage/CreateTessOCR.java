package tran.getImage;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.MalformedURLException;
import java.net.URL;

import tran.calculatetipsocr.MainScreen;
import tran.computeTip.DisplayAmount;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.content.res.AssetManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Matrix;
import android.net.Uri;
import android.os.AsyncTask;
import android.widget.Toast;

import com.googlecode.tesseract.android.TessBaseAPI;
import com.googlecode.tesseract.android.TessBaseAPI.PageSegMode;

/**
 * @purpose Class to invoke OCR to get the amount to tip to the user.
 * @author Todd
 * @since 1.8
 */
public class CreateTessOCR {

	/**
	 * the context of the main screen
	 */
	private Context c_MainActivityContext;
	
	/**
	 * byte array containing the user's image
	 */
	private byte[] ba_ArrayOfImage;
	
	/**
	 * object to allow access to get the tessdata from the assets folder.
	 */
	private AssetManager am_AssetsObject;
	
	/**
	 * tessdata directory
	 */
	private String s_theTessdataDir;
	
	/**
	 * the receipt directory
	 */
	private String s_theReceiptDir;
	
	/**
	 * The amount to rotate the image by
	 */
	int i_rotationAmount;

	/**
	 * constructor to invoke class methods.
	 */
	public CreateTessOCR(Context c_MainActivityContext, AssetManager am_AssetsObject) {
		this.c_MainActivityContext = c_MainActivityContext;
		this.am_AssetsObject = am_AssetsObject;
	}
	
	/**
	 * sets the directory for the tessdata.eng file
	 * @param s_theDir The directory of the tessdata.eng file
	 */
	public void sets_theTessdataDir(String s_theDir) {
		this.s_theTessdataDir = s_theDir;
	}
	
	/**
	 * @return The string representing where the tessdata.eng file is.
	 */
	public String gets_theTessdataDir() {
		return this.s_theTessdataDir;
	}
	
	/**
	 * Sets the rotation amount for the image taken
	 * @param i_rotationAmount The amount to rotate the image taken
	 */
	public void seti_rotationAmount(int i_rotationAmount) {
		this.i_rotationAmount = i_rotationAmount;
	}
 	
	/**
	 * @return gets the rotation amount
	 */
	public int geti_rotationAmount() {
		return this.i_rotationAmount;
	}

	/**
	 * Sets the path to access the receipt image
	 * @param s_theReceiptPath The path of the image taken.
	 */
	public void sets_theReceiptDir(String s_theReceiptPath) {
		this.s_theReceiptDir = s_theReceiptPath;
	}
	
	/**
	 * @return the path to access the receipt image
	 */
	public String gets_theReceiptDir() {
		return this.s_theReceiptDir;
	}
	
	/**
	 * Sets the byte array with the picture taken
	 * @param ba_arrayOfPicture The picture taken as a byte array.
	 */
	public void setba_ArrayOfImage(byte[] ba_arrayOfPicture) {
		this.ba_ArrayOfImage = ba_arrayOfPicture;
	}
	
	/**
	 * @return The byte array with the picture contents.
	 */
	public byte[] getba_ArrayOfImage() {
		return this.ba_ArrayOfImage;
	}
	
	
	/**
	 * helper function to write from the assets folder into internal storage if the tessdata is not there.
	 */
	public void writeToInternalStorage() {
		// check to see if there is 
		// 1) getApplicationInfo().dataDir + "data";
		// 2) use the assessmanager.open to get the eng.trainneddata (assume english lang)
		// 3) store the file from 2) into an inputstream
		// 4) write the contents of 3) into a fileoutputstream line by line.	
		File f_tessDataObject = new File(s_theTessdataDir  + "/tessdata/eng.traineddata");
		if(!f_tessDataObject.exists()) {// check for an existing tessdata file 
			new File(s_theTessdataDir  + "/tessdata/").mkdir(); // create the /tessdata directory into internal storage.
			OutputStream ops_createFile = null;
			InputStream is_getFile = null;
			int length = 0;
			byte[] buffer = new byte[1024];
			try {
				is_getFile = am_AssetsObject.open("tessdata/eng.traineddata"); // grab the file from the assets directory (part of the .apk generated file)
				ops_createFile = new FileOutputStream(s_theTessdataDir  + "/tessdata/eng.traineddata");
				
				while ((length = is_getFile.read(buffer)) > 0) {
					ops_createFile.write(buffer, 0, length);
			    }
				
				ops_createFile.flush();
				ops_createFile.close();
				is_getFile.close();
			} 
			catch (IOException e1) {
				// TODO Auto-generated catch block
				e1.printStackTrace();
				Toast.makeText(c_MainActivityContext, "file doesn't exist!", Toast.LENGTH_SHORT).show();
			}
		}
	}
	
	public void processImageInBackground() {
		writeToInternalStorage();
		
		ProcessImageResults pir_InvokeBackgroundWork = new ProcessImageResults();
		pir_InvokeBackgroundWork.execute();
		
		
	}
	
	/**
	 * An AsyncTask to perform image processing in the background.
	 * @author Todd
	 */
	private class ProcessImageResults extends AsyncTask<Void, Void, String> {

		private ProgressDialog dialog;

		public ProcessImageResults() {}
		
		@Override
		protected void onPreExecute()
		{ 
		   super.onPreExecute();

		   dialog = new ProgressDialog(c_MainActivityContext);
		   dialog.setMessage("Computing amount to pay.");
		   dialog.show(); 
		   dialog.setCanceledOnTouchOutside(false); // user cannot click to cancel this.
	  
		}
		
		@Override
		protected String doInBackground(Void... params) 
		{
			return readFromOnlineFile();
			//return refactorImageTaken();
		}
		
		@Override
	    protected void onPostExecute(String result) 
		{
			// display the text data.
		  	//Toast.makeText(c_MainActivityContext, result, Toast.LENGTH_SHORT).show();
		  	// parse stuff from this text after printing it...
		  	//dialog.dismiss();
			
			// first select number of people paying and the tax %.
			// dialog 1 (amount of people paying)
			
			// dialog 2 (tip %)
			
			// compute the amount to pay.
			DisplayAmount da_computeCost = new DisplayAmount();
			String amountToTip = da_computeCost.practiceParser(result, MainScreen.i_numPayers, MainScreen.d_tipPercentAmount);
			
			// display the total
			MainScreen.tv_theTotal.setText(amountToTip);
			dialog.dismiss();
		}
		
		/**
		 * reads from a remote file.
		 * @purpose: this is a debugging function, the bitmap is grabbed from a file online and not taken from the camera.
		 */
		public String readFromOnlineFile() {
		  	//String s_theTestLink = "http://htsvt.heliohost.org/imagesForOCR/Kohls_Receipt.jpg";
			//String s_theTestLink = "http://htsvt.heliohost.org/imagesForOCR/eurotext.png";
			String s_theTestLink = "http://htsvt.heliohost.org/imagesForOCR/best_your_Receipt.jpg";
			Bitmap bm_thePictureTest = null;
			InputStream is = null;
			try {
				is = (InputStream) new URL(s_theTestLink).getContent();
				bm_thePictureTest = BitmapFactory.decodeStream(is);
			  	is.close();
		  	}
		  	catch (MalformedURLException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			
		  	TessBaseAPI tbp_tesserActObjectbaseApi = new TessBaseAPI();
		  	tbp_tesserActObjectbaseApi.init(s_theTessdataDir, "eng");
		  	
		  	tbp_tesserActObjectbaseApi.setImage(bm_thePictureTest);
		  	
		  	tbp_tesserActObjectbaseApi.setPageSegMode(PageSegMode.PSM_AUTO_OSD);

		    String s_textFromPicture = tbp_tesserActObjectbaseApi.getUTF8Text();
		  	bm_thePictureTest.recycle();
		  	return s_textFromPicture;
		}

		/**
		 * http://developer.android.com/training/displaying-bitmaps/load-bitmap.html
		 * re-scales the image to prevent an outOfMemory error.
		 */
		public String refactorImageTaken() {
			
			BitmapFactory.Options options = new BitmapFactory.Options();
			options.inJustDecodeBounds = true;
			BitmapFactory.decodeByteArray(ba_ArrayOfImage, 0, ba_ArrayOfImage.length, options);
			String imageType = options.outMimeType;
			options.inSampleSize = 2;
			options.inJustDecodeBounds = false;
			Bitmap bm_scaledPicture = BitmapFactory.decodeByteArray(ba_ArrayOfImage, 0, ba_ArrayOfImage.length, options);
			
			Matrix matrix = new Matrix(); 
			matrix.postRotate(i_rotationAmount); 
			Bitmap bm_rotatedPicture = Bitmap.createBitmap(bm_scaledPicture, 0, 0, bm_scaledPicture.getWidth(), bm_scaledPicture.getHeight(), matrix, true);
			bm_scaledPicture.recycle();
			
			saveTheReceiptScaled(bm_rotatedPicture); // save the receipt so the image can be emailed to the user.
		
		    // for now not emailing the receipt to the user.
			emailTheReceipt();
			
			// get text for postExecute() to print to the user the total
			return getTextFromImage(bm_rotatedPicture);
			
		}
		
		/**
		 * performs OCR on the bitmap to extract relevant text
		 * @param bm_thePicture The picture to run OCR on
		 */
		public String getTextFromImage(Bitmap bm_thePicture) {
			TessBaseAPI tbp_tesserActObjectbaseApi = new TessBaseAPI();
		  	tbp_tesserActObjectbaseApi.init(s_theTessdataDir, "eng");
		  	
		  	tbp_tesserActObjectbaseApi.setImage(bm_thePicture);
		  	
		  	tbp_tesserActObjectbaseApi.setPageSegMode(PageSegMode.PSM_AUTO_OSD);
		  	//tbp_tesserActObjectbaseApi.setPageSegMode(PageSegMode.PSM_AUTO);
		  	
		    String s_textFromPicture = tbp_tesserActObjectbaseApi.getUTF8Text();
		    bm_thePicture.recycle();
		    return s_textFromPicture;
		}
		
		/**
		 * helper function to save the picture in case the user wants to email it.
		 * @param bm_pictureToSave The picture to save.
		 */
		public void saveTheReceiptScaled(Bitmap bm_pictureToSave) {
			File f_picturesDirectory = new File(s_theReceiptDir);
			FileOutputStream fos_outputStream = null;
			if(!f_picturesDirectory.exists()) {
				f_picturesDirectory.mkdir();
			}
			
			File f_thePicture = new File(f_picturesDirectory, "/your_Receipt.jpg"); // the image file.
			try {
				fos_outputStream = new FileOutputStream(f_thePicture);
				bm_pictureToSave.compress(Bitmap.CompressFormat.JPEG, 100, fos_outputStream);
				fos_outputStream.close();
			}
			catch(IOException e) {
				e.printStackTrace();
			}
		}
		
		/**
		 * helper function to email the receipt if the user chooses to do so.
		 */
		public void emailTheReceipt() {
			File f_thePicture = new File(s_theReceiptDir, "/your_Receipt.jpg"); // the image file.

			Intent i_emailIntent = new Intent(android.content.Intent.ACTION_SEND); 
			i_emailIntent.setType("image/jpg");
			// emailing to myself to show the image.
			i_emailIntent.putExtra(android.content.Intent.EXTRA_EMAIL, new String[]{"toddtran9@gmail.com"}); 
			i_emailIntent.putExtra(android.content.Intent.EXTRA_SUBJECT, "Image From Tessteract Android App"); 
			i_emailIntent.putExtra(android.content.Intent.EXTRA_TEXT, "An image of your receipt is attached below"); 
			i_emailIntent.putExtra(Intent.EXTRA_STREAM, Uri.fromFile(f_thePicture));
			c_MainActivityContext.startActivity(i_emailIntent);

		}
	
		
	}
}
