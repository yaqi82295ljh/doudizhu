package com.ddz.game.pattern;

import static com.ddz.game.pattern.PatternType.BOMB;
import static com.ddz.game.pattern.PatternType.ROCKET;

public class PatternComparator {

    public static boolean canBeat(CardPattern mine, CardPattern last){

        if (last == null){
            return true;
        }

        if (last.type() == ROCKET){
            return false;
        }

        if (mine.type() == ROCKET){
            return true;
        }

        if (mine.type() == BOMB && last.type() != BOMB){
            return true;
        }

        if (mine.type() == BOMB && last.type() == BOMB){
            if(mine.mainRank() > last.mainRank()){
                return true;
            }else {
                return false;
            }
        }

        if (mine.type() != BOMB && last.type() == BOMB){
            return false;
        }

        if (mine.type() != last.type() || mine.count() != last.count()){
            return false;
        }

        if (mine.mainRank() > last.mainRank()){
            return true;
        }else {
            return false;
        }

    }


}
