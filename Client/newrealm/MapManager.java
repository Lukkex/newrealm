package newrealm;

import java.util.*;
import tage.GameObject;
import tage.input.action.*;
import net.java.games.input.Event;

public class MapManager {
    private int map1Width =  9;
    private int map1Height = 20;
    private int map2Width = 16;
    private int map2Height = 25;
    private ArrayList<Map> maps = new ArrayList<Map>();

    private class Map {
        private int mapID;
        private char[][] layout;
        private int width, height;
        private int numOfLockedDoors;
        private int[] playerSpawnLocation = new int[2];

        public Map(){} //Empty map

        public Map(int mapID, int width, int height){
            this.mapID = mapID;
            this.width = width;
            this.height = height;
        }

        public void setMapLayout(char[][] layout){
            this.layout = layout;

            //Set Player location
            for (int i = 0; i < this.width; i++){
                for (int j = 0; j < this.height; j++){
                    if (layout[i][j] == 'P'){
                        playerSpawnLocation[0] = i;
                        playerSpawnLocation[1] = j;
                        return; 
                    }
                }
            }

            playerSpawnLocation = null;
        }

        public char[][] getMapLayout(){
            return this.layout;
        }

        public int getMapWidth(){
            return this.width;
        }

        public int getMapHeight(){
            return this.height;
        }

        public int[] getPlayerLocation(){
            return this.playerSpawnLocation;
        }

        public void setKeyDoorPair(int mapID, int x, int y){

        }
    }

    public MapManager(){
        //Generate Game Map #1
        //0 is empty space
        //1 is wall
        //8 is window wall
        //9 is invisible wall
        //2 is door West-East
        //3 is door North-South
        //4 is door key
        //5 is chest
        //6 is chest key
        //G is ghoul enemy
        //F is flying enemy
        //A is aeon knight enemy
        //P is where player starts
        //@ is next level

        char[][] temp  = {
            {1,  1,  1,  1,  1,  1, 8, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1},
            {8,  0,  8,  0,  5,  1, 0, 0, 1, 1, 1, 0, 0, 0, 0, 0, 0, 'G', 1, 1},
            {1,  0,  1,  3,  8,  1, 1, 3, 1, 1, 1, 0, 8, 0, 8, 0, 8, 0, 1, 1},
            {1,  0,  1,  0,  0,  1, 1, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 1},
            {8,  0,  2,  0, 'G', 8, 8, 0, 0, 0, 0, 0, 0, 0, 'G', 0, 0, 0, '@', 1},
            {1,  0,  1,  0,  4,  1, 1, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 1},
            {1,  0,  1,  3,  8,  1, 1, 3, 1, 1, 1, 0, 8, 0, 8, 0, 8, 0, 1, 1},
            {8, 'P', 8,  0,  0,  0, 0, 0, 1, 1, 1, 0, 0, 0, 0, 0, 0, 'G', 1, 1},
            {1,  3,  1,  1,  1,  1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1}
        };

        Map map1 = new Map(1, map1Width, map1Height);
        map1.setMapLayout(temp);

        maps.add(new Map()); //Empty map; will make it so Map #1 is at index 1 and not 0
        maps.add(map1);

        char[][] temp2  = {
            {1, 1,  1,  1,  1,  1,  1,  1,  1,  1,  1,  1,  1,  1,  1,  1,  1,  1,  1,  1,  1,  1,  1,  1,  1},
            {1, 0,  0,  0,  0,  0,  0,  0,  0,  0,  0,  0,  0,  0,  0,  0,  0,  0,  0,  0,  0,  0,  0,  0,  1},
            {1, 0,  0,  0,  0,  0,  0,  0,  0,  0,  0,  0,  0,  0,  0,  0,  0,  0,  0,  0,  0,  0,  0,  0,  1},
            {1, 0,  0,  0,  0,  0,  0,  0,  0,  0,  0,  0,  0,  0,  0,  0,  0,  0,  0,  0,  0,  0,  0,  0,  1},
            {1, 0,  0,  0,  0,  0,  0,  0,  0,  0,  0,  0,  0,  0,  0,  0,  0,  0,  0,  0,  0,  0,  0,  0,  1},
            {1, 0,  0,  0,  0,  0,  0,  0,  0,  0,  0,  0,  0,  0,  0,  0,  0,  0,  0,  0,  0,  0,  0,  0,  1},
            {1, 0,  0,  0,  0,  0,  0,  0,  0,  0,  0,  0,  0,  0,  0,  0,  0,  0,  0,  0,  0,  0,  0,  0,  1},
            {1, 0,  0,  0,  0,  0,  0,  0,  0,  'P',  0,  0,  0,  0,  0,  0,  0,  0,  0,  0,  0,  0,  0,  0,  1},
            {1, 0,  0,  0,  0,  0,  0,  0,  0,  0,  0,  0,  0,  0,  0,  0,  0,  0,  0,  0,  0,  0,  0,  0,  1},
            {1, 0,  0,  0,  0,  0,  0,  0,  0,  0,  0,  0,  0,  0,  0,  0,  0,  0,  0,  0,  0,  0,  0,  0,  1},
            {1, 0,  0,  0,  0,  0,  0,  0,  0,  0,  0,  0,  0,  0,  0,  0,  0,  0,  0,  0,  0,  0,  0,  0,  1},
            {1, 0,  0,  0,  0,  0,  0,  0,  0,  0,  0,  0,  0,  0,  0,  0,  0,  0,  0,  0,  0,  0,  0,  0,  1},
            {1, 0,  0,  0,  0,  0,  0,  0,  0,  0,  0,  0,  0,  0,  0,  0,  0,  0,  0,  0,  0,  0,  0,  0,  1},
            {1, 0,  0,  0,  0,  0,  0,  0,  0,  0,  0,  0,  0,  0,  0,  0,  0,  0,  0,  0,  0,  0,  0,  0,  1},
            {1, 0,  0,  0,  0,  0,  0,  0,  0,  0,  0,  0,  0,  0,  0,  0,  0,  0,  0,  0,  0,  0,  0,  0,  1},
            {1, 1,  1,  1,  1,  1,  1,  1,  1,  1,  1,  1,  1,  1,  1,  1,  1,  1,  1,  1,  1,  1,  1,  1,  1}
        };

        Map map2 = new Map(2, map2Width, map2Height);
        map2.setMapLayout(temp2);

        maps.add(new Map()); //Empty map; will make it so Map #1 is at index 1 and not 0
        maps.add(map2);
    }

    public char[][] getGameMap(int mapID){
        return maps.get(mapID).getMapLayout();
    }

    /** Returns the state of the specified x, y location in the specified map */
    public int getMapLocationState(int mapID, int x, int y){
        return maps.get(mapID).getMapLayout()[x][y];
    }

    public int getMapWidth(int mapID){
        return maps.get(mapID).getMapWidth();
    }

    public int getMapHeight(int mapID){
        return maps.get(mapID).getMapHeight();
    }

    public int[] getPlayerLocation(int mapID){
        return maps.get(mapID).getPlayerLocation();
    }
}