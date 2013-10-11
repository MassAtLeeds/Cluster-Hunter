/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package uk.ac.leeds.mass.cluster;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

/**
 *
 * @author geo8kh
 */
public class IdentifyRecordsFromClusters extends JSONReader{

    private CoordinateFactory geog = Parameters.getCurrent().getCoordinateFactory(0.0, 0.0);
    private ArrayList<SignificantCircle> results;
    
    private BufferedWriter bw = null;
    private boolean write = true;
    
    public IdentifyRecordsFromClusters(File file, ArrayList<SignificantCircle> results) throws IOException, JSONException{
        super(file);
        this.results = results;

        File outputDirectory = Parameters.getCurrent().getOutputDirectory();
        
        if ( !outputDirectory.exists() ){
            write = outputDirectory.mkdirs();
        }else if ( !outputDirectory.isDirectory() ){
            write = false;
            Logger.log("Specified output location is not a directory : " + outputDirectory.getAbsolutePath(), 
                    Logger.messageSeverity.Information, "IdentifyRecordsFromClusters");
        }

        //get the name of the input file
        String csv = Parameters.getCurrent().getCSV().getName();

        String outputName = "";
        if ( csv.endsWith(".csv") ){outputName = csv.substring(0, csv.length()-4);}
        File output = new File(outputDirectory + System.getProperty("file.separator") + outputName + ".JSON");

        if (output.exists()){output.delete();}
        output.createNewFile();
        bw = new BufferedWriter(new FileWriter(output));
        
        super.readFile();
        
        JSONArray out = new JSONArray();
        //for each significant circle write out a JSON object to the output file
        for (SignificantCircle significantCircle : results) {
            JSONObject obj = new JSONObject();
            obj.put("clusterID", significantCircle.getID());
            obj.put("lat", significantCircle.getY());
            obj.put("lon", significantCircle.getX());
            obj.put("radius", significantCircle.getRadius());
            obj.put("sign", significantCircle.getValue());
            ArrayList<Long> points = significantCircle.getTweets();
            JSONArray a = new JSONArray();
            //create an array of points for all of the tweet IDs contributing to this circle
            for (Long long1 : points) {
                a.put(long1.longValue());
            }
            obj.put("points", a);
//            obj.write(bw);
//            try{bw.newLine();}catch(IOException e){
                //swallow any exception here it isn't that important, at worst we will 
                //not have a clean line break between two JSON objects, should still be 
                //machine readable.
//            }
            out.put(obj);
        }                
        
        out.write(bw);
        
        bw.flush();
        bw.close();
        
    }
    
    @Override
    protected void doActionWithValidCoordinates(double x, double y) {
        
        
        geog.setX(x);
        geog.setY(y);
        
        
        //write out the results
        if (write && isEvent){
            

             double radius = 0.0;
             boolean radiusChanged = true;
             double bbYmin = 0.0;
             double bbYmax = 0.0;
             double bbXmin = 0.0;
             double bbXmax = 0.0;

             for (SignificantCircle significantCircle : results) {

                 double rad = significantCircle.getRadius();
                 radiusChanged = (rad!=radius);
                 if ( radiusChanged ){
                     radius = rad;
                     bbYmin = geog.getOffsetXY(-(radius * 1.05 ), false);
                     bbYmax = geog.getOffsetXY((radius * 1.05 ), false);
                     bbXmin = geog.getOffsetXY(-(radius * 1.05 ), true);
                     bbXmax = geog.getOffsetXY((radius * 1.05 ), true);
                 }

                 if ( significantCircle.getX() > bbXmin && significantCircle.getX() < bbXmax && 
                         significantCircle.getY() > bbYmin && significantCircle.getY() < bbYmax){
                     //point is inside the bounding box so test the distance
                     if ( geog.dist(significantCircle.getX(), significantCircle.getY()) < radius ){
                        //add the tweet ID to the significant circle
                         significantCircle.addTweet(new Long(tweetID));
                     }
                 }
             }
            
        }
    }
    
}