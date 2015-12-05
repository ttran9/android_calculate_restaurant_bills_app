package tran.calculatetipsocr;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.MalformedURLException;
import java.net.URL;

import tran.getImage.CreateTessOCR;
import android.app.Activity;
import android.app.AlertDialog.Builder;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.res.AssetManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Matrix;
import android.hardware.Camera;
import android.hardware.Camera.CameraInfo;
import android.hardware.Camera.Parameters;
import android.hardware.Camera.PictureCallback;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Environment;
import android.view.Menu;
import android.view.MenuItem;
import android.view.Surface;
import android.view.SurfaceHolder;
import android.view.SurfaceHolder.Callback;
import android.view.SurfaceView;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.googlecode.tesseract.android.TessBaseAPI;
import com.googlecode.tesseract.android.TessBaseAPI.PageSegMode;

/**
 * @purpose Main screen for the user to take a picture and then extract words to come up with an amount to tip.
 * @author Todd Tran
 * @since 1.8
 */
@SuppressWarnings("deprecation") // take away warnings of the Camera API being deprecated.
public class MainScreen extends Activity implements Callback {

	/**
	 * view to hold the previewed receipt image
	 */
	private SurfaceView sv_PreviewedImage;
	
	/**
	 * interface to allow pixels on the previewed image
	 */
	private SurfaceHolder sh_ImageHolder;
	
	/**
	 * the camera object.
	 */
	private Camera c_theCamera;
	
	/**
	 * callback object for when the picture is taken.
	 */
	private PictureCallback jpegCallback;
	
	/**
	 * button to indicate to take the picture
	 */
	private Button b_takePicture;
	
	/**
	 * text field where user can determine number of payers.
	 */
	private TextView tv_payerDescription;
	
	/**
	 * text field where user can determine percentage to tip.
	 */
	private TextView tv_tipPercentAmount;
	
	/**
	 * path to the picture.
	 */
	private String s_thePathOfTheReceipt;
	
	/**
	 * path to the tessdata file.
	 */
	private String s_tessDataPath;
	
	/**
	 * object to invoke image processing.
	 */
	private CreateTessOCR ctc_ImageProcessObject;
	
	/**
	 * the context of this activity.
	 */
	private Context c_TheActivityContext;
	
	/**
	 * object to hold the picture to be processed.
	 */
	private Bitmap bm_thePicture;
	
	/**
	 * keeps track of the id of the open camera.
	 */
	private int i_idOfOpenCamera;
	
	/**
	 * the degrees to rotate the camera by.
	 */
	private int i_rotationAmount;
	
	/**
	 * the total to pay after factoring in the tip
	 */
	public static TextView tv_theTotal;
	
	/**
	 * the number of people paying
	 */
	public static int i_numPayers;
	
	/**
	 * the amount to tip
	 */
	public static double d_tipPercentAmount;
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main_screen);
		
		sv_PreviewedImage = (SurfaceView) findViewById(R.id.surfaceOfPicture);
		sh_ImageHolder = sv_PreviewedImage.getHolder();
		
		sh_ImageHolder.addCallback(this);
		sh_ImageHolder.setType(SurfaceHolder.SURFACE_TYPE_PUSH_BUFFERS);
		
		c_TheActivityContext = this;
		i_rotationAmount = 0;
		
		s_thePathOfTheReceipt = Environment.getExternalStoragePublicDirectory(
	            Environment.DIRECTORY_PICTURES + "/pictures").toString();
		
		s_tessDataPath = getApplicationInfo().dataDir;
		
		ctc_ImageProcessObject = new CreateTessOCR(this, getAssets());
		
		tv_theTotal = (TextView) findViewById(R.id.theTotal);
		//tv_theTotal.setText("You will pay: " + "$39.18");
		i_numPayers = 1;
		d_tipPercentAmount = 20;
		
		
		// define a callback for when the picture is taken.
	    jpegCallback = new PictureCallback() {
	          
	          @Override
	          public void onPictureTaken(byte[] data, Camera camera) {
	        	  ctc_ImageProcessObject.setba_ArrayOfImage(data);
	        	  ctc_ImageProcessObject.seti_rotationAmount(i_rotationAmount);
	        	  ctc_ImageProcessObject.sets_theReceiptDir(s_thePathOfTheReceipt);
	        	  ctc_ImageProcessObject.sets_theTessdataDir(s_tessDataPath);
	        	  ctc_ImageProcessObject.processImageInBackground();
	        	  
	        	  refreshCamera();
	          }
	    };
	    
	    b_takePicture = (Button) findViewById(R.id.takePicture);
	    b_takePicture.setOnClickListener(new OnClickListener() {
             public void onClick(View v) {
            	 // change this to enforce auto focusing?????
            	 c_theCamera.takePicture(null, null, jpegCallback);
             }
        });
	}
	
	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.main_screen, menu);
		return true;
	}

	/**
	 * modifies class variables based on what menu items are selected
	 * side note: this will need to be modified in a later version?
	 */
	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		// Handle action bar item clicks here. The action bar will
		// automatically handle clicks on the Home/Up button, so long
		// as you specify a parent activity in AndroidManifest.xml.
		int i_theItemId = item.getItemId();
		switch(i_theItemId) {
			case R.id.numPayOne:
				i_numPayers	= 1;
				break;
			case R.id.numPayTwo:
				i_numPayers	= 2;
				break;
			case R.id.numPayThree:
				i_numPayers	= 3;
				break;
			case R.id.numPayFour:
				i_numPayers	= 4;
				break;
			case R.id.numPayFive:
				i_numPayers	= 5;
				break;
			case R.id.numPayOther:
				// pop up a dialog and get a value
				getInputFromUser(0);
				break;
			case R.id.tipOne:
				d_tipPercentAmount = 10;
				break;
			case R.id.tipTwo:
				d_tipPercentAmount = 15;
				break;
			case R.id.tipThree:
				d_tipPercentAmount = 20;
				break;
			case R.id.tipFour:
				d_tipPercentAmount = 25;
				break;
			case R.id.tipFive:
				d_tipPercentAmount = 30;
				break;
			case R.id.tipOther:
				// pop up a dialog and get a value
				getInputFromUser(1);
				break;
		}

		return super.onOptionsItemSelected(item);
	}
	
	/**
	 * helper function to create the pop up dialog that takes in user input.
	 * @param i_fieldToSet This value determines if the tip amount or number of users paying is being set.
	 */
	public void getInputFromUser(final int i_fieldToSet) {
		// grab the customized popup layout.
		View v_popupMenu = getLayoutInflater().inflate(R.layout.select_values_popup, new LinearLayout(c_TheActivityContext), false);
		Builder b_popupBuilder = new Builder(this);
		
		final EditText et_userInput = (EditText) v_popupMenu.findViewById(R.id.userInput);
		TextView tv_theTitleToSet = (TextView) v_popupMenu.findViewById(R.id.inputTitle);
		
		if(i_fieldToSet == 0) {
			tv_theTitleToSet.setText("Enter number of people paying");
		}
		else {
			tv_theTitleToSet.setText("Enter the tip percent amount");
		}
		
		// use the layout of the customized pop-up.
		b_popupBuilder.setView(v_popupMenu);
		
		b_popupBuilder.setPositiveButton("Ok", new DialogInterface.OnClickListener() {
			public void onClick(DialogInterface dialog, int id) {
				// get user input and set it to result
				if(i_fieldToSet == 0) {
					i_numPayers = Integer.parseInt(et_userInput.getText().toString());
				}
				else {
					d_tipPercentAmount = Double.parseDouble(et_userInput.getText().toString());
				}
            }
        })
        .setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int id) {
            	dialog.cancel();
            }
        });
		
		b_popupBuilder.show();
	}
	
    /**
     * helper function to refresh the camera once the picture has been taken.
     */
    public void refreshCamera() {
        if (sh_ImageHolder.getSurface() == null) {
           return;
        }
        
        try {
           c_theCamera.stopPreview();
        }
        catch (Exception e) {
        }
        
        try {
        	c_theCamera.setPreviewDisplay(sh_ImageHolder);
        	c_theCamera.startPreview();
        	
         	Parameters p_cameraParameters = c_theCamera.getParameters();
	    	//p_cameraParameters.setFocusMode(Parameters.FOCUS_MODE_AUTO);
	    	p_cameraParameters.setFocusMode(Parameters.FOCUS_MODE_CONTINUOUS_PICTURE);
	    	c_theCamera.setParameters(p_cameraParameters);
	    	
        }
        catch (Exception e) {
        	e.printStackTrace();
        }
     }
    
    /**
     * get id of the open camera
     */
    public void setOpenCameraID() {
    	int numberOfCameras = Camera.getNumberOfCameras();
        CameraInfo cameraInfo = new CameraInfo();
        for (int i_theCameraID = 0; i_theCameraID < numberOfCameras; i_theCameraID++) {
            Camera.getCameraInfo(i_theCameraID, cameraInfo);
            if (cameraInfo.facing == CameraInfo.CAMERA_FACING_BACK) {
            	i_idOfOpenCamera = i_theCameraID;
            }
        }
        return ;
    }
    
    /**
     * sets the orientation of the camera.
     */
    public void setCameraOrientation() {
        CameraInfo ci_cameraInfo = new Camera.CameraInfo();
        Camera.getCameraInfo(i_idOfOpenCamera, ci_cameraInfo);
        int rotation = getWindowManager().getDefaultDisplay()
                .getRotation();
        int degrees = 0;
        switch (rotation) {
            case Surface.ROTATION_0: degrees = 0; break;
            case Surface.ROTATION_90: degrees = 90; break;
            case Surface.ROTATION_180: degrees = 180; break;
            case Surface.ROTATION_270: degrees = 270; break;
        }
        
        if (ci_cameraInfo.facing == Camera.CameraInfo.CAMERA_FACING_FRONT) {
        	i_rotationAmount = (ci_cameraInfo.orientation + degrees) % 360;
        	i_rotationAmount = (360 - i_rotationAmount) % 360;  // compensate the mirror
        } else {  // back-facing
        	i_rotationAmount = (ci_cameraInfo.orientation - degrees + 360) % 360;
        }
        c_theCamera.setDisplayOrientation(i_rotationAmount);
    }
    
	// below methods will help manipulate the sv_PreviewedImage object of this class.
	@Override
	public void surfaceCreated(SurfaceHolder holder) {
		// TODO Auto-generated method stub		
		try {
			// open back camera.
			c_theCamera = Camera.open();
			setOpenCameraID();
			setCameraOrientation(); //
		}
		catch(RuntimeException e) { // to catch if the back camera is already in use by other apps
			e.printStackTrace();
		}
	    try {
	    	c_theCamera.setPreviewDisplay(sh_ImageHolder);
	    	c_theCamera.startPreview();
	    	
	    	Parameters p_cameraParameters = c_theCamera.getParameters();
	    	p_cameraParameters.setFocusMode(Parameters.FOCUS_MODE_CONTINUOUS_PICTURE);
	    	c_theCamera.setParameters(p_cameraParameters);
	    	/*
	    	c_theCamera.autoFocus(new AutoFocusCallback() {
				@Override
				public void onAutoFocus(boolean success, Camera camera) {
					// TODO Auto-generated method stub
					 c_theCamera.takePicture(null, null, jpegCallback);
				}
	    	});
	    	*/
        }
	    catch (Exception e) {
	    	System.err.println(e);
	    	return;
	    }
	}

	@Override
	public void surfaceChanged(SurfaceHolder holder, int format, int width,
			int height) {
		// TODO Auto-generated method stub
	}

	@Override
	public void surfaceDestroyed(SurfaceHolder holder) {
		// TODO Auto-generated method stub
		// free up the camera so it can be used in other applications.
		c_theCamera.stopPreview();
		c_theCamera.release();
		c_theCamera = null;
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
		File f_tessDataObject = new File(getApplicationInfo().dataDir  + "/tessdata/eng.traineddata");
		if(!f_tessDataObject.exists()) {// check for an existing tessdata file 
			new File(getApplicationInfo().dataDir  + "/tessdata/").mkdir(); // create the /tessdata directory into internal storage.
			AssetManager am_objectToGetAssets = getAssets();
			OutputStream ops_createFile = null;
			InputStream is_getFile = null;
			int length = 0;
			byte[] buffer = new byte[1024];
			try {
				is_getFile = am_objectToGetAssets.open("tessdata/eng.traineddata"); // grab the file from the assets directory (part of the .apk generated file)
				ops_createFile = new FileOutputStream(getApplicationInfo().dataDir  + "/tessdata/eng.traineddata");
				
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
				Toast.makeText(c_TheActivityContext, "file doesn't exist!", Toast.LENGTH_SHORT).show();
			}
		}
	}
	
	/**
	 * An AsyncTask to perform image processing in the background.
	 * @author Todd
	 */
	private class ProcessImageResults extends AsyncTask<Void, Void, String> {
	
		//private ProgressDialog dialog;
		
		private byte[] ba_pictureContent;
		
		public ProcessImageResults(byte[] ba_pictureContent) {
			this.ba_pictureContent = ba_pictureContent;
		}
		
		@Override
		protected void onPreExecute()
		{ 
		   super.onPreExecute();
		   //dialog = new ProgressDialog(c_TheActivityContext);
		   //dialog.setMessage("Processing Your Image");
		   //dialog.show();    
		}
		
		@Override
		protected String doInBackground(Void... params) 
		{
			//selectPicture();
			return extractDataFromPicture(ba_pictureContent);
		}
		
		@Override
	    protected void onPostExecute(String result) 
		{
			//bm_thePicture.recycle(); // chkin xxx, to prevent out of memory errors.
			// display the text data.
		  	//Toast.makeText(c_TheActivityContext, result, Toast.LENGTH_SHORT).show();
			//Toast.makeText(c_TheActivityContext, "Height: " + i_heightOfPicture + "\n" + "Width: " + i_widthOfPicture + "\n", Toast.LENGTH_SHORT).show();
		  	refreshCamera(); 
		  	// parse stuff from this text after printing it...
		  	//dialog.dismiss();
		}
		
		/**
		 * helper function taking the picture from a jpg file and extracting relevant information from it.
		 * @param ba_thePictureContents The picture from the camera.
		 */
		public String extractDataFromPicture(byte[] ba_thePictureContents) {
			//Bitmap bm_PictureFromFile = BitmapFactory.decodeFile(s_thePathOfTheReceipt + "/1_your_Receipt.jpg");
			/*
			Bitmap bm_PictureFromFile = BitmapFactory.decodeFile(s_thePathOfTheReceipt + "/your_Receipt.jpg");
			
			TessBaseAPI tbp_tesserActObjectbaseApi = new TessBaseAPI();
		  	tbp_tesserActObjectbaseApi.init(getApplicationInfo().dataDir, "eng");
		  	
		  	tbp_tesserActObjectbaseApi.setImage(bm_PictureFromFile);
		  	
		  	//tbp_tesserActObjectbaseApi.setImage(codec(bm_thePicture, Bitmap.CompressFormat.JPEG, 100));
		  	
		  	//tbp_tesserActObjectbaseApi.setPageSegMode(PageSegMode.PSM_SPARSE_TEXT);
		  	//tbp_tesserActObjectbaseApi.setPageSegMode(PageSegMode.PSM_SINGLE_LINE);
		  	//tbp_tesserActObjectbaseApi.setPageSegMode(PageSegMode.PSM_SPARSE_TEXT_OSD);
		  	tbp_tesserActObjectbaseApi.setPageSegMode(PageSegMode.PSM_AUTO_OSD);
		  	//tbp_tesserActObjectbaseApi.setPageSegMode(PageSegMode.PSM_OSD_ONLY);
		  	
		    String s_textFromPicture = tbp_tesserActObjectbaseApi.getUTF8Text();
		    bm_PictureFromFile.recycle();
		    //
			// now do the emailing.
		    //
			Intent i_emailIntent = new Intent(android.content.Intent.ACTION_SEND); 
			i_emailIntent.putExtra(android.content.Intent.EXTRA_EMAIL, new String[]{"toddtran9@gmail.com"}); 
			i_emailIntent.putExtra(android.content.Intent.EXTRA_SUBJECT, "Image From Tessteract Android App"); 
			i_emailIntent.putExtra(android.content.Intent.EXTRA_TEXT, s_textFromPicture); 
			startActivity(i_emailIntent);
		  	return s_textFromPicture;	
			*/
			/*
			bm_thePicture = BitmapFactory.decodeByteArray(ba_thePictureContents, 0, ba_thePictureContents.length);
			
		  	TessBaseAPI tbp_tesserActObjectbaseApi = new TessBaseAPI();
		  	tbp_tesserActObjectbaseApi.init(getApplicationInfo().dataDir, "eng");
		  	
		  	tbp_tesserActObjectbaseApi.setImage(bm_thePicture);
		  	//tbp_tesserActObjectbaseApi.setImage(codec(bm_thePicture, Bitmap.CompressFormat.JPEG, 100));
		  	
		  	//tbp_tesserActObjectbaseApi.setPageSegMode(PageSegMode.PSM_SPARSE_TEXT);
		  	tbp_tesserActObjectbaseApi.setPageSegMode(PageSegMode.PSM_SINGLE_LINE);
		  	
		    String s_textFromPicture = tbp_tesserActObjectbaseApi.getUTF8Text();
		  	
		  	return s_textFromPicture;	
			*/
			
			//return readFromOnlineFile(ba_thePictureContents);
		  	
		  	// send the taken picture to an email.
		  	
		  	return sendImageTaken(ba_thePictureContents);
		  	
		  	//return "";
		  	
		}
		
		/**
		 * reads from a remote file. won't actually be used.
		 */
		public String readFromOnlineFile(byte[] ba_thePictureContents) {
		  	//String s_theTestLink = "http://htsvt.heliohost.org/imagesForOCR/Kohls_Receipt.jpg";
			String s_theTestLink = "http://htsvt.heliohost.org/imagesForOCR/eurotext.png";
			InputStream is = null;
			try {
				is = (InputStream) new URL(s_theTestLink).getContent();
		  		bm_thePicture = BitmapFactory.decodeStream(is);
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
		  	tbp_tesserActObjectbaseApi.init(getApplicationInfo().dataDir, "eng");
		  	
		  	tbp_tesserActObjectbaseApi.setImage(bm_thePicture);
		  	
		  	tbp_tesserActObjectbaseApi.setPageSegMode(PageSegMode.PSM_SPARSE_TEXT);
		  	
		    String s_textFromPicture = tbp_tesserActObjectbaseApi.getUTF8Text();
		  	
		  	return s_textFromPicture;
		}
		
		/**
		 * http://developer.android.com/training/displaying-bitmaps/load-bitmap.html
		 * takes the picture from the camera and sends it to an email. won't be used in the final app.
		 */
		public String sendImageTaken(byte[] ba_thePictureContents) {
			BitmapFactory.Options options = new BitmapFactory.Options();
			options.inJustDecodeBounds = true;
			BitmapFactory.decodeByteArray(ba_thePictureContents, 0, ba_thePictureContents.length, options);
			String imageType = options.outMimeType;
			options.inSampleSize = 2;
			options.inJustDecodeBounds = false;
			bm_thePicture = BitmapFactory.decodeByteArray(ba_thePictureContents, 0, ba_thePictureContents.length, options);
			
			
			Matrix matrix = new Matrix(); 
			matrix.postRotate(i_rotationAmount); 
			Bitmap bm_rotatedPicture = Bitmap.createBitmap(bm_thePicture, 0, 0, bm_thePicture.getWidth(), bm_thePicture.getHeight(), matrix, true);
		    bm_thePicture.recycle();
			
			saveTheReceiptScaled(bm_rotatedPicture);
			emailTheReceipt();
			
			
			TessBaseAPI tbp_tesserActObjectbaseApi = new TessBaseAPI();
		  	tbp_tesserActObjectbaseApi.init(getApplicationInfo().dataDir, "eng");
		  	
		  	tbp_tesserActObjectbaseApi.setImage(bm_rotatedPicture);
		  	
		  	tbp_tesserActObjectbaseApi.setPageSegMode(PageSegMode.PSM_AUTO_OSD);
		  	
		    String s_textFromPicture = tbp_tesserActObjectbaseApi.getUTF8Text();
		    //
		    bm_rotatedPicture.recycle();
		    
			//return "";
		    return s_textFromPicture;
			////
		    /*
			//
			File f_picturesDirectory = new File(s_thePathOfTheReceipt);
			FileOutputStream fos_outputStream = null;
			if(!f_picturesDirectory.exists()) {
				f_picturesDirectory.mkdir();
			}
			
			File f_thePicture = new File(f_picturesDirectory, "/your_Receipt.jpg"); // the image file.

			try {
				fos_outputStream = new FileOutputStream(f_thePicture);
				bm_rotatedPicture.compress(Bitmap.CompressFormat.JPEG, 100, fos_outputStream);
				bm_rotatedPicture.recycle();
				fos_outputStream.close();
				// now do the emailing.
				Intent i_emailIntent = new Intent(android.content.Intent.ACTION_SEND); 
				i_emailIntent.setType(imageType);
				i_emailIntent.putExtra(android.content.Intent.EXTRA_EMAIL, new String[]{"toddtran9@gmail.com"}); 
				i_emailIntent.putExtra(android.content.Intent.EXTRA_SUBJECT, "Image From Tessteract Android App"); 
				i_emailIntent.putExtra(android.content.Intent.EXTRA_TEXT, "The attachment of the picture"); 
				i_emailIntent.putExtra(Intent.EXTRA_STREAM, Uri.fromFile(f_thePicture));
				startActivity(i_emailIntent);
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			*/
		}
		
		
		/**
		 * helper function to save the picture in case the user wants to email it.
		 * @param bm_pictureToSave The picture to save.
		 */
		public void saveTheReceiptScaled(Bitmap bm_pictureToSave) {
			File f_picturesDirectory = new File(s_thePathOfTheReceipt);
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
			File f_thePicture = new File(s_thePathOfTheReceipt, "/your_Receipt.jpg"); // the image file.

			Intent i_emailIntent = new Intent(android.content.Intent.ACTION_SEND); 
			i_emailIntent.setType("image/jpg");
			i_emailIntent.putExtra(android.content.Intent.EXTRA_EMAIL, new String[]{"toddtran9@gmail.com"}); 
			i_emailIntent.putExtra(android.content.Intent.EXTRA_SUBJECT, "Image From Tessteract Android App"); 
			i_emailIntent.putExtra(android.content.Intent.EXTRA_TEXT, "The attachment of the picture"); 
			i_emailIntent.putExtra(Intent.EXTRA_STREAM, Uri.fromFile(f_thePicture));
			startActivity(i_emailIntent);

		}
		
	}
		
}
