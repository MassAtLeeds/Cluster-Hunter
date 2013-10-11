/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package uk.ac.leeds.mass.cluster;

import java.util.ArrayList;

/**
 *
 * @author geo8kh
 */
public class Gamk {
    
    Data data = null;
    
    private double xMin = 0.0;
    private double xMax = 0.0; 
    private double yMin = 0.0;
    private double yMax = 0.0;
    private double height = 0.0;
    private double width = 0.0;
    
    private double radMin = Parameters.getCurrent().getRadiusMin();
    private double radMax = Parameters.getCurrent().getRadiusMax();
    private double radInc = Parameters.getCurrent().getRadiusIncrement();
    private double overlp = Parameters.getCurrent().getRadiusOverlap();

    private PoissonTest sigTest = null;
            
    private ArrayList<SignificantCircle> results = new ArrayList<SignificantCircle>();
    
    private CoordinateFactory geog = null;
    
    // Constructors
    public Gamk(Data data) {
        this.data = data;
    }

    
    public ArrayList<SignificantCircle> getResults(){return results;}
    
    @Override
    public String toString() {return "Gamk" + data.toString();}
    
    private void setSearchBounds(){
        double[][] searchData = data.getData();
        for (int i = 0; i < searchData[1].length; i++) {
            //first pass through so set all of the variables
            if ( i == 0 ){
                xMin = searchData[1][i];
                xMax = searchData[1][i];
                yMin = searchData[2][i];
                yMax = searchData[2][i];
            }else{
                if (searchData[1][i] < xMin){xMin = searchData[1][i];}
                if (searchData[1][i] > xMax){xMax = searchData[1][i];}
                if (searchData[2][i] < yMin){yMin = searchData[2][i];}
                if (searchData[2][i] > yMax){yMax = searchData[2][i];}
            }
        }
        
        //get the height
        geog = Parameters.getCurrent().getCoordinateFactory(xMin, yMin);
        height = geog.dist(xMin, yMax);
        //get the width
        width = geog.dist(xMax, yMin);
        
    }   
        
    private double
	rangeN, // Northing range
	rangeE; // Easting range
    private int
	minPnt ,// Minimum point count
	totCal,
	totDat,
	totNHy,
	totNC2;
    int id =1;
 
		
    public void gamAlgorithm(){
        
//        double minPnt = pars.getMinPointCount();
        
        // Find max and min x, y values to define search region
        setSearchBounds();
        
        //setup the significance test
        sigTest = new PoissonTest(0,
                data.getData()[0].length,
                Parameters.getCurrent().getStatisticType(),
                Parameters.getCurrent().getMinimumPopulationCount(),
                Parameters.getCurrent().getMinimumCaseCount(),
                Parameters.getCurrent().getMultipleTestReRuns(),
                Parameters.getCurrent().getSignificanceThreshold());

//        rangeN = maxN - minN;
//        rangeE = maxE - minE;
//
//
//        // adjust region size for radius of max circle
//        float yDiff = (float) (xMaxN + 2.0 * radMax + 1.0 - xMinN);
//        float xDiff = (float)(xMaxE + 2.0 * radMax + 1.0 - xMinE);
//        float range = Misc.max(nDiff, eDiff);
 


//    public void search(){
        
        totCal = totDat = totNC2 = totNHy = 0;
        
        //get the number of times the algorithm has to step through the different size circles
        int rTimes = (int) ((radMax-radMin)/radInc + 1.0);
        double radius = radMin - radInc;
//        double width=rec.getWidth();
//        double height=rec.getHeight();
        
//        double trad=radius;
//        for (int i = 0;i<rTimes;i++){
//            trad +=radInc;
//            TotalCalc+=(int)((width/(trad*overlp))*(height/(trad*overlp)));
//        }
        for (int loop=0; loop < rTimes; loop++){
            radius += radInc;
//            MyInt nSigCircs = new MyInt();
//            MyInt nHyps = new MyInt();
            sequentialSearch(radius);//L,radius,nSigCircs, nHyps, 0,rec);
        }

    }
    
    int nCalc = 0;//,TotalCalc=0;

    public void sequentialSearch(double radius){//Vector L, double radius, MyInt numCals, 
				 //MyInt numHy, int sample,GeoRectangle rec){
        
        //data to be searched
        double[][] searchData = data.getData();
        
        int nDat, nCals, nHy;
        nDat = nCals = nHy = 0;

        //get the increment required after each search
        double step = radius * overlp;
//        double radSq = radius * radius;
        //get the number of searches on the y axis
        int yTimes = (int) (height/step + 1.0);
        //get the number of searches on the x axis
        int xTimes = (int) (width/step + 1.0);
        
//        //X and Y for the search circle
//        double cX = xMin;
//        double cY = yMin;
	

        //make sure the Y coordinate is at the minimum of the search area
        geog.setY(yMin);
        
        //set the bouding box variables for the current circle
        double bbYmin = geog.getOffsetXY(-(radius * 1.05 ), false);
        double bbYmax = geog.getOffsetXY((radius * 1.05 ), false);
        
        double bbXmin = 0.0;
        double bbXmax = 0.0;
        
    	// Grid search: northern loop
        for (int iRow=0; iRow < yTimes; iRow++){

            geog.setX(xMin);
            bbXmin = geog.getOffsetXY(-(radius * 1.05 ), true);
            bbXmax = geog.getOffsetXY((radius * 1.05 ), true);
	  
            // grid search: easting loop
            for (int iCol = 0; iCol < xTimes; iCol++){
//                if(cl!=null&&!pars.getAnimate()) cl.setPos(cX,cY);

                nCalc++;
                //get data for circle
                double obsP = 0.0;
                double obsC = 0.0;
                int pointsInRadius = 0;
                for (int i = 0; i < searchData[0].length; i++) {
                    //only test the distance if the coordinates fall inside the bounding box
                    //of the circle to test
                    if ( searchData[1][i] > bbXmin && searchData[1][i] < bbXmax && searchData[2][i] > bbYmin && searchData[2][i] < bbYmax){
                        //point is inside the bounding box so test the distance
                        if ( geog.dist(searchData[1][i], searchData[2][i]) < radius ){
                           obsP += searchData[4][i];
                           obsC += searchData[3][i];
                           pointsInRadius++;
                        }
                    }
                }
                
                //no points found so go straight to the next circle
                if(pointsInRadius==0) continue;
                
//                for(int i=0;i<l2;i++){
//                    obsP += ((MyPoint)points.elementAt(i)).getPop();
//                    obsC += ((MyPoint)points.elementAt(i)).getCases();
//                }

                if (obsC > 0){
                    int j = 0;
                }
                
                nDat++;
                if (sigTest.isWorthTesting(obsP, obsC)){
                    nHy++;
                    //debug.print(" tested, ");
                    if (sigTest.isSignificant(obsP, obsC)){

                        nCals ++;

                        results.add(new SignificantCircle(geog.getX(),geog.getY(), radius, sigTest.getStat()));
                        
                    }// end if  significant

                }// end of if worth testing
		
                //increment the x coordinate for the search
                geog.offsetXY(step, true);
                bbXmin = geog.getOffsetXY(-(radius * 1.05 ), true);
                bbXmax = geog.getOffsetXY((radius * 1.05 ), true);
            }//end of x loop
            
            //increment the y coordinate for the search
            geog.offsetXY(step, false);
            bbYmin = geog.getOffsetXY(-(radius * 1.05 ), false);
            bbYmax = geog.getOffsetXY((radius * 1.05 ), false);
            
        }// end of y loop


        // form global stats
        totCal += nCalc;
        totDat += nDat;
        totNHy += nHy;
        totNC2 += nCals;

//        numCals.x = nCals;
//        numHy.x = nHy;
//        if(cl!=null) cl.setPos(Integer.MAX_VALUE,Integer.MAX_VALUE);
//        return;
    }

   
}
