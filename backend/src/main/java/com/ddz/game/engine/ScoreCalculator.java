package com.ddz.game.engine;

public class ScoreCalculator {

    //单局分 = baseScore × 地主叫分 × 2^bombCount
    public static int score(GameState st) {
        int score = st.baseScore * st.bids[st.landlord] * (1 << st.bombCount);
        return score;
    }

    public static int[] settle(GameState st){
        int[] scores = {0,0,0};
        int fen = score(st);

        if (st.landlord == st.winner){
            scores[st.landlord] = 2* fen;
            for (int i = 0; i < scores.length; i++){
                if (i != st.landlord){
                    scores[i] = -fen;
                }
            }
        }else {
            scores[st.landlord] = -(2* fen);
            for (int i = 0; i < scores.length; i++){
                if (i != st.landlord){
                    scores[i] = fen;
                }
            }
        }

        return scores;
    }

}
