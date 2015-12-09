package edu.msu.elhazzat.whirpool.routing;


import android.app.Activity;
import android.content.Context;
import android.graphics.Color;

import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Polyline;
import com.google.android.gms.maps.model.PolylineOptions;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;

import edu.msu.elhazzat.whirpool.R;

/**
 * Created by Stephanie on 10/13/2015.
 */
public class MapRoute extends Activity {

    private GoogleMap mmap;
    private String mBuildingName;
    private int mFloorNum;

    private List<DijkstraVertex> mNodes;
    private List<DijkstraEdge> mEdges;
    private ArrayList<LatLng> mCoordsList = new ArrayList<>();

    //rawEdges stores the edges from the text files (the node corresponding with an index shares edges with these int[] nodes)
    private ArrayList<int[]> mRawEdges = new ArrayList<>();

    private int mEndPointIndex = 0;
    private int mStartPointIndex = 0;
    private double mLatDifference = 0.0;
    private double mLonDifference = 0.0;
    private double mLeastDifference = 100.0;

    private Context mContext;
    private InputStream mInStream;

    private boolean mShowRoute = true;
    private boolean mElevatorStart = false;
    private boolean mElevatorEnd = false;

    private boolean mDirectRoute = true;
    private int mEndFloor;
    private LatLng mEndPoint;
    private LatLng mStartPoint;

    private boolean mThreeStageRoute = false;


    //constructor
    public MapRoute(Context context, GoogleMap map, String buildingName, int currentFloorNum) {
        mContext = context;
        mmap = map;
        mBuildingName = buildingName;
        mFloorNum = currentFloorNum;
    }


    //main function to call, requires desired start & end points + their respective floors
    public Polyline drawRoute(LatLng startPoint, int startFloor, LatLng endPoint, int endFloor){

        mEndFloor = endFloor;
        mEndPoint = endPoint;
        mStartPoint = startPoint;

       /* System.out.println("Start POINT: " + startPoint);
        System.out.println("START FLOOR: " + startFloor);
        System.out.println("End POINT: " + endPoint);
        System.out.println("End FLOOR: " + endFloor);
        System.out.println("Current FLOOR: " + mFloorNum);*/

        populateNodes();
        populateEdges();

        //handles navigation between floors
        //elevator will ALWAYS be the one closest to the user's start point
        if(startFloor != endFloor || mFloorNum != endFloor){

            if(!mThreeStageRoute) {
                //user's map is currently on the starting floor, end point becomes elevator
                if (startFloor == mFloorNum) {
                    if (startFloor == 1)
                        mEndPointIndex = getElevatorIndex(endPoint);
                    else
                        mEndPointIndex = getElevatorIndex(startPoint);
                    mElevatorEnd = true;
                }

                //user's map is currently on the ending floor, start point becomes elevator
                else if (endFloor == mFloorNum) {
                    mStartPointIndex = getElevatorIndex(startPoint);
                    mElevatorStart = true;
                } else if (endFloor != mFloorNum) {
                    mEndPointIndex = getElevatorIndex(endPoint);
                    mStartPointIndex = getElevatorIndex(startPoint);
                    mElevatorStart = true;
                    mElevatorEnd = true;
                }
            }
            else{
                mStartPointIndex = getElevatorIndex(endPoint);
                mElevatorStart = true;
            }


        }

        //determine which node is closest to end point coordinates
        //actual end point will be that node, except when map is not on the end point's floor
        if(!mElevatorEnd){
            for (int i = 0; i < mCoordsList.size(); i++) {

                mLatDifference = Math.abs(mCoordsList.get(i).latitude - endPoint.latitude);
                mLonDifference = Math.abs(mCoordsList.get(i).longitude - endPoint.longitude);

                if (Math.sqrt(mLatDifference*mLatDifference + mLonDifference*mLonDifference) < mLeastDifference) {
                    mLeastDifference = mLatDifference + mLonDifference;
                    mEndPointIndex = i;
                }

            }
        }

        //reset variable
        mLeastDifference = 100.0;


        //determine which node is closest to start point coordinates
        //actual start point will be that node, except when map is not on the start point's floor
        if(!mElevatorStart) {

            for (int i = 0; i < mCoordsList.size(); i++) {

                mLatDifference = Math.abs(mCoordsList.get(i).latitude - startPoint.latitude);
                mLonDifference = Math.abs(mCoordsList.get(i).longitude - startPoint.longitude);

                if (Math.sqrt(mLatDifference*mLatDifference + mLonDifference*mLonDifference) < mLeastDifference) {
                    mLeastDifference = mLatDifference + mLonDifference;
                    mStartPointIndex = i;
                }

            }

        }

        //make sure start and end point are not the same
        if(mStartPointIndex == mEndPointIndex) {

            if(mStartPointIndex + 1 < mCoordsList.size())
                mStartPointIndex += 1;

            else
                mStartPointIndex -= 1;

        }

        //set up the graph
        DijkstraGraph graph = new DijkstraGraph(mNodes, mEdges);
        DijkstraAlgorithm dijkstra = new DijkstraAlgorithm(graph);

        //starting node
        dijkstra.execute(mNodes.get(mStartPointIndex));

        //get path to end node
        LinkedList<DijkstraVertex> path = dijkstra.getPath(mNodes.get(mEndPointIndex));

        if(path == null){
            if(endFloor == startFloor && !mThreeStageRoute)
                mEndPointIndex = getElevatorIndex(startPoint);
            else if(endFloor == mFloorNum){
                mStartPointIndex = getElevatorIndex(endPoint);
            }
            else
                mEndPointIndex = getElevatorIndex(endPoint);

            //make sure start and end point are not the same
            if(mStartPointIndex == mEndPointIndex) {

                if(mStartPointIndex + 1 < mCoordsList.size())
                    mStartPointIndex += 1;

                else
                    mStartPointIndex -= 1;

            }

            //starting node
            dijkstra.execute(mNodes.get(mStartPointIndex));

            //get path to end node
            path = dijkstra.getPath(mNodes.get(mEndPointIndex));

            mDirectRoute = false;

        }

        //set up the line
        PolylineOptions line = new PolylineOptions().width(5).color(Color.BLUE);

        //if map is displaying a floor other than the start/end floor, the line will be invisible for now
        if(!mShowRoute) line.visible(false);

        //add the correct points to the line
        for (DijkstraVertex vertex : path) {
            line.add(vertex.coords);
        }
     //   line.add(endPoint);

        //draw the line
        Polyline pLine = mmap.addPolyline(line);

        //return the line for easy removal (pLine.remove())
        return pLine;

    }


    //returns index of elevator that is closest to the start point
    private int getElevatorIndex(LatLng startPoint){

        double distanceFromStart;
        double secondDistance;
        double thirdDistance;

        LatLng coordinate;

        switch(mBuildingName){
            case("GHQ"):
                switch (mFloorNum){
                    case(1):
                        //node 38 and 37

                        coordinate = mCoordsList.get(38);

                        distanceFromStart = Math.sqrt((coordinate.latitude - startPoint.latitude) * (coordinate.latitude - startPoint.latitude) +
                                (coordinate.longitude - startPoint.longitude) * (coordinate.longitude - startPoint.longitude));

                        coordinate = mCoordsList.get(37);

                        secondDistance = Math.sqrt((coordinate.latitude - startPoint.latitude)*(coordinate.latitude - startPoint.latitude) +
                                (coordinate.longitude - startPoint.longitude)*(coordinate.longitude - startPoint.longitude));

                        if(secondDistance < distanceFromStart)
                            return  37;
                        else
                            return 38;

                    case(2):
                        //node 26 and 47

                        coordinate = mCoordsList.get(26);

                        distanceFromStart = Math.sqrt((coordinate.latitude - startPoint.latitude) * (coordinate.latitude - startPoint.latitude) +
                                (coordinate.longitude - startPoint.longitude) * (coordinate.longitude - startPoint.longitude));

                        coordinate = mCoordsList.get(47);

                        secondDistance = Math.sqrt((coordinate.latitude - startPoint.latitude)*(coordinate.latitude - startPoint.latitude) +
                                (coordinate.longitude - startPoint.longitude)*(coordinate.longitude - startPoint.longitude));

                        if(secondDistance < distanceFromStart)
                            return  47;
                        else
                            return 26;

                    case(3):
                        //node 28 and 7

                        coordinate = mCoordsList.get(28);

                        distanceFromStart = Math.sqrt((coordinate.latitude - startPoint.latitude) * (coordinate.latitude - startPoint.latitude) +
                                (coordinate.longitude - startPoint.longitude) * (coordinate.longitude - startPoint.longitude));

                        coordinate = mCoordsList.get(7);

                        secondDistance = Math.sqrt((coordinate.latitude - startPoint.latitude)*(coordinate.latitude - startPoint.latitude) +
                                (coordinate.longitude - startPoint.longitude)*(coordinate.longitude - startPoint.longitude));

                        if(secondDistance < distanceFromStart)
                            return  7;
                        else
                            return 28;

                    case(4):
                        //node 26 and 7

                        coordinate = mCoordsList.get(26);

                        distanceFromStart = Math.sqrt((coordinate.latitude - startPoint.latitude) * (coordinate.latitude - startPoint.latitude) +
                                (coordinate.longitude - startPoint.longitude) * (coordinate.longitude - startPoint.longitude));

                        coordinate = mCoordsList.get(7);

                        secondDistance = Math.sqrt((coordinate.latitude - startPoint.latitude)*(coordinate.latitude - startPoint.latitude) +
                                (coordinate.longitude - startPoint.longitude)*(coordinate.longitude - startPoint.longitude));

                        if(secondDistance < distanceFromStart)
                            return  7;
                        else
                            return 26;
                }
                break;

            case("RV"):
                switch(mFloorNum){
                    case(1):
                        //nodes 28 98 173

                        coordinate = mCoordsList.get(28);

                        distanceFromStart = Math.sqrt((coordinate.latitude - startPoint.latitude) * (coordinate.latitude - startPoint.latitude) +
                                (coordinate.longitude - startPoint.longitude) * (coordinate.longitude - startPoint.longitude));

                        coordinate = mCoordsList.get(98);

                        secondDistance = Math.sqrt((coordinate.latitude - startPoint.latitude) * (coordinate.latitude - startPoint.latitude) +
                                (coordinate.longitude - startPoint.longitude) * (coordinate.longitude - startPoint.longitude));

                        coordinate = mCoordsList.get(173);

                        thirdDistance = Math.sqrt((coordinate.latitude - startPoint.latitude)*(coordinate.latitude - startPoint.latitude) +
                                (coordinate.longitude - startPoint.longitude)*(coordinate.longitude - startPoint.longitude));

                        if(distanceFromStart < secondDistance && distanceFromStart < thirdDistance)
                            return 28;

                        else if(secondDistance < distanceFromStart && secondDistance < thirdDistance)
                            return 98;

                        else
                            return 173;

                    case(2):
                        //nodes 26 107 151

                        coordinate = mCoordsList.get(26);

                        distanceFromStart = Math.sqrt((coordinate.latitude - startPoint.latitude) * (coordinate.latitude - startPoint.latitude) +
                                (coordinate.longitude - startPoint.longitude) * (coordinate.longitude - startPoint.longitude));

                        coordinate = mCoordsList.get(107);

                        secondDistance = Math.sqrt((coordinate.latitude - startPoint.latitude) * (coordinate.latitude - startPoint.latitude) +
                                (coordinate.longitude - startPoint.longitude) * (coordinate.longitude - startPoint.longitude));

                        coordinate = mCoordsList.get(151);

                        thirdDistance = Math.sqrt((coordinate.latitude - startPoint.latitude)*(coordinate.latitude - startPoint.latitude) +
                                (coordinate.longitude - startPoint.longitude)*(coordinate.longitude - startPoint.longitude));

                        if(distanceFromStart < secondDistance && distanceFromStart < thirdDistance)
                            return 26;

                        else if(secondDistance < distanceFromStart && secondDistance < thirdDistance)
                            return 107;

                        else
                            return 151;

                    case(3):
                        //nodes 7 and 96

                        coordinate = mCoordsList.get(7);

                        distanceFromStart = Math.sqrt((coordinate.latitude - startPoint.latitude) * (coordinate.latitude - startPoint.latitude) +
                                (coordinate.longitude - startPoint.longitude) * (coordinate.longitude - startPoint.longitude));

                        coordinate = mCoordsList.get(96);

                        secondDistance = Math.sqrt((coordinate.latitude - startPoint.latitude)*(coordinate.latitude - startPoint.latitude) +
                                (coordinate.longitude - startPoint.longitude)*(coordinate.longitude - startPoint.longitude));

                        if(distanceFromStart < secondDistance)
                            return 7;
                        else
                            return  96;
                }
                break;
        }
        return 0;
    }


    //read in all of the navigation coordinates, create nodes
    private void populateNodes(){

        mNodes = new ArrayList<DijkstraVertex>();
        mEdges = new ArrayList<DijkstraEdge>();

        switch(mBuildingName){
            case("GHQ"):
                switch(mFloorNum){
                    case(1):
                        mInStream = mContext.getResources().openRawResource(R.raw.ghq_nw_f1_nav);
                        break;
                    case(2):
                        mInStream = mContext.getResources().openRawResource(R.raw.ghq_nw_f2_nav);
                        break;
                    case(3):
                        mInStream = mContext.getResources().openRawResource(R.raw.ghq_nw_f3_nav);
                        break;
                    case(4):
                        mInStream = mContext.getResources().openRawResource(R.raw.ghq_nw_f4_nav);
                        break;
                }
                break;

            case("RV"):
                switch (mFloorNum){
                    case(1):
                        mInStream = mContext.getResources().openRawResource(R.raw.rv_f1_nav);
                        break;
                    case(2):
                        mInStream = mContext.getResources().openRawResource(R.raw.rv_f2_nav);
                        break;
                    case(3):
                        mInStream = mContext.getResources().openRawResource(R.raw.rv_f3_nav);
                        break;
                }
                break;
        }

        InputStreamReader mInStreamReader = new InputStreamReader(mInStream);
        BufferedReader reader = new BufferedReader(mInStreamReader);

        String line;

        //parsing stuff
        try {
            while ((line = reader.readLine()) != null) {
                float lat;
                float lon;
                String[] parsedLine;

                line = line.replace("[", "");
                line = line.replace("]", "");
                parsedLine = line.split(",");
                lat = Float.parseFloat(parsedLine[1]);
                lon = Float.parseFloat(parsedLine[2]);
                mCoordsList.add(new LatLng(lat, lon));

                //index 0 will be node number, the rest will be its edges
                int[] nodeEdges = new int[parsedLine.length - 2];
                nodeEdges[0] = Integer.parseInt(parsedLine[0]);

                for(int i = 3; i < parsedLine.length; i++){
                    nodeEdges[i-2] = Integer.parseInt(parsedLine[i]);
                }
                mRawEdges.add(nodeEdges);
            }

        }
        catch (IOException e){

        }

        //add each parsed coordinate as a node
        for(int i = 0; i < mCoordsList.size(); i++){
            DijkstraVertex location = new DijkstraVertex("Node_" + i, "Node_" + i, mCoordsList.get(i));
            mNodes.add(location);
        }

    }

    //create the traversable edges between nodes
    private void populateEdges(){

        for(int i = 0; i < mRawEdges.size(); i++){
            for(int x = 1; x < mRawEdges.get(i).length; x++){
                DijkstraEdge lane = new DijkstraEdge("Edge_" + i, mNodes.get(mRawEdges.get(i)[0]), mNodes.get(mRawEdges.get(i)[x]));
                mEdges.add(lane);
            }
        }

    }

    public boolean checkDirectRoute(){

        if(mElevatorEnd && !mElevatorStart && mFloorNum != 1){

            mFloorNum = mEndFloor;
            mCoordsList = new ArrayList<>();
            mRawEdges = new ArrayList<>();

            populateNodes();
            populateEdges();

            mStartPointIndex = getElevatorIndex(mStartPoint);

            for (int i = 0; i < mCoordsList.size(); i++) {

                mLatDifference = Math.abs(mCoordsList.get(i).latitude - mEndPoint.latitude);
                mLonDifference = Math.abs(mCoordsList.get(i).longitude - mEndPoint.longitude);

                if (Math.sqrt(mLatDifference*mLatDifference + mLonDifference*mLonDifference) < mLeastDifference) {
                    mLeastDifference = mLatDifference + mLonDifference;
                    mEndPointIndex = i;
                }

            }

            //set up the graph
            DijkstraGraph graph = new DijkstraGraph(mNodes, mEdges);
            DijkstraAlgorithm dijkstra = new DijkstraAlgorithm(graph);

            //starting node
            dijkstra.execute(mNodes.get(mStartPointIndex));

            //get path to end node
            LinkedList<DijkstraVertex> path = dijkstra.getPath(mNodes.get(mEndPointIndex));

            if(path == null)
                mDirectRoute = false;

        }

        return mDirectRoute;
    }

    public void thirdStageRoute(){
        mThreeStageRoute = true;
    }



}
