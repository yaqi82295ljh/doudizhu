package com.ddz.game.engine;

import com.ddz.game.card.Card;
import com.ddz.game.move.Move;
import com.ddz.game.card.Deck;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

public class GameState {

    List<List<Card>> hands;         //三家手牌
    List<Card> bottom;              //3 张底牌
    int landlord = -1;              //地主座位号
    int current;                    //当前该谁出牌
    Move lastMove;                  //上家出的牌
    int lastPlayer;                 //上家是谁
    Phase phase = Phase.CALLING;

    int[] bids = {-1,-1,-1};    //叫分,抢地主时用
    int winner = -1;            //胜利者
    int baseScore = 1;          // 底分
    int bombCount = 0;          // 炸弹，王炸出现次数

    public List<Card> getHand(int seat)   { return hands.get(seat); }
    public Move getLastMove()             { return lastMove; }
    public int getCurrent()               { return current; }
    public int getLandlord()              { return landlord; }
    public int getWinner()                { return winner; }
    public Phase getPhase()               { return phase; }
    public int getBombCount()             { return bombCount; }
    public int getBaseScore()             { return baseScore; }
    public int getBid(int seat)           { return bids[seat]; }
    public int getLastPlayer() { return lastPlayer; }




    public enum Phase {
        CALLING,
        PLAYING,
        FINISHED;
    }

    public GameState(List<List<Card>> hands, List<Card> bottom, int landlord, Phase phase,Move lastMove) {
        this.hands = hands;
        this.bottom = bottom;
        this.landlord = landlord;
        this.lastMove = lastMove;
        this.phase = phase;

    }

    //游戏初始化
    public static GameState start(){
        List<List<Card>> all = Deck.deal(new Random());
        List<List<Card>> hands = new ArrayList<>();
        hands.add(all.get(0));
        hands.add(all.get(1));
        hands.add(all.get(2));
        List<Card> bottom = all.get(3);

        Move lastMove = null;
        Phase phase = Phase.CALLING;
        int landlord = -1;

        return new GameState(hands,bottom,landlord,phase,lastMove);
    }

    // 判断叫分是否结束
    public boolean bid(int seat, int score) {
        bids[seat] = score;

        if (score == 3) {
            return true;
        }

        for (int b : bids) {
            if (b == -1) {
                return false;
            }
        }
        return true;
    }

    // 找到分最高的人，决定谁是地主，没有就流局
    public int settleLandlord() {
        int max = -1;
        for (int i = 0; i < 3; i++) {
            max = Math.max(max, bids[i]);
        }

        if (max <= 0) {
            return -1;
        }

        for (int i = 0; i < 3; i++) {
            if (bids[i] == max) {
                setLandlord(i);
                return i;
            }
        }
        return -1;
    }


    //设置地主
    public void setLandlord(int seat){
        this.landlord = seat;
        List<Card> addAll = hands.get(landlord);
        hands.get(landlord).addAll(bottom);
        this.current = landlord;
        this.phase = Phase.PLAYING;
    }


}
