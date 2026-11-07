package com.ddz.game.engine;

import com.ddz.game.card.Card;
import com.ddz.game.move.Move;
import com.ddz.game.pattern.CardPattern;
import com.ddz.game.pattern.PatternComparator;
import com.ddz.game.pattern.PatternParser;
import com.ddz.game.pattern.PatternType;

import java.util.List;

public class TurnManager {

    //校验轮次 → 从手牌移除 → 更新 lastMove/lastPlayer → 传给下家

    public static void play(GameState st, int seat, Move move) {
        if (st.phase != GameState.Phase.PLAYING){
            return;
        }
        if (seat != st.current){
            return;
        }

        List<Card> hand = st.hands.get(seat);
        if (!hand.containsAll(move.cards())){
            return;
        }

        //判断牌型是否合法
        CardPattern real = PatternParser.parse(move.cards()).orElse(null);
        if (real == null){
            return;
        }

        //判断是否压的过
        if (st.lastMove != null && !PatternComparator.canBeat(real, st.lastMove.pattern())){
            return;
        }

        hand.removeAll(move.cards());
        if (real.type().equals(PatternType.ROCKET) ||real.type().equals(PatternType.BOMB)){
            st.bombCount++;
        }
        st.lastMove = new Move(move.cards(), real);
        st.lastPlayer = seat;
        st.current = (seat + 1) % 3;

        if (hand.isEmpty()) {
            st.phase = GameState.Phase.FINISHED;
            st.winner = seat;
        }
    }



    //过牌
    public static void pass(GameState st, int seat) {
        if (st.phase != GameState.Phase.PLAYING){
            return;
        }
        if (seat != st.current){
            return;
        }
        if (st.lastMove == null){
            return;
        }

        st.current = (seat + 1) % 3;

        if (st.current == st.lastPlayer) {
            st.lastMove = null;
        }
    }






}
