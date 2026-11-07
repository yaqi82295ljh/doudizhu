package com.ddz.ai;

import com.ddz.game.card.Card;
import com.ddz.game.engine.GameState;
import com.ddz.game.move.Move;
import com.ddz.game.move.MoveGenerator;
import com.ddz.game.pattern.PatternType;
import java.util.List;


public class AiPlayer {

    public Move decide(GameState st, int seat) {

        if (st.getLastMove() != null){
            if(st.getLandlord() != seat && st.getLandlord() != st.getLastPlayer()) {
                return null;
            }
        }

        List<Card> hand = st.getHand(seat);
        List<Move> candidates = (st.getLastMove() == null)
                ? MoveGenerator.allMoves(hand)
                : MoveGenerator.beatingMoves(hand, st.getLastMove().pattern());

        Move best = null;
        for (Move m : candidates) {
            if (best == null || weight(m) < weight(best)) {
                best = m;
            }
        }
        return best;
    }

    private static int weight(Move m) {
        PatternType t = m.pattern().type();
        if (t == PatternType.ROCKET){
            return 1000;
        }
        if (t == PatternType.BOMB){
            return 500 + m.pattern().mainRank();
        }
        return m.pattern().mainRank();
    }


}
