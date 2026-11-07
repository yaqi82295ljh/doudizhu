package com.ddz.ai;

import com.ddz.game.card.Card;
import com.ddz.game.engine.GameState;
import com.ddz.game.engine.TurnManager;
import com.ddz.game.move.Move;
import com.ddz.game.pattern.PatternParser;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;

class TeamTmpTest {

    private static List<Card> cards(String... ids) {
        List<Card> l = new ArrayList<>();
        for (String id : ids) l.add(Card.of(id));
        return l;
    }

    private static Move moveOf(String... ids) {
        List<Card> c = cards(ids);
        return new Move(c, PatternParser.parse(c).orElseThrow());
    }

    private static GameState fixture(String[] h0, String[] h1, String[] h2) {
        List<List<Card>> hands = new ArrayList<>();
        hands.add(cards(h0));
        hands.add(cards(h1));
        hands.add(cards(h2));
        return new GameState(hands, new ArrayList<>(), 0, GameState.Phase.PLAYING, null);
    }

    @Test
    void probe() {
        AiPlayer ai = new AiPlayer();

        System.out.println("=== 1. 农民压农民（地主=0）===");
        GameState st1 = fixture(new String[]{"S5", "H4"}, new String[]{"S9", "H4"}, new String[]{"S10", "H4"});
        TurnManager.play(st1, 0, moveOf("S5"));   // 地主出 S5
        TurnManager.play(st1, 1, moveOf("S9"));   // 农民1 出 S9
        Move m1 = ai.decide(st1, 2);              // 农民2 该不该压农民1？
        System.out.println("农民2 decide -> " + (m1 == null ? "null（让队友）" : m1.cards().toString())
                + "  (应 null)");

        System.out.println("=== 2. 农民压地主 ===");
        GameState st2 = fixture(new String[]{"S5", "H4"}, new String[]{"S9", "H4"}, new String[]{"S10", "H4"});
        TurnManager.play(st2, 0, moveOf("S5"));
        Move m2 = ai.decide(st2, 1);              // 农民1 面对地主
        System.out.println("农民1 decide -> " + (m2 == null ? "null" : m2.cards().toString())
                + "  (应出 S9)");

        System.out.println("=== 3. 农民压不过地主 ===");
        GameState st3 = fixture(new String[]{"SA", "H4"}, new String[]{"S3", "H4"}, new String[]{"S10", "H4"});
        TurnManager.play(st3, 0, moveOf("SA"));
        Move m3 = ai.decide(st3, 1);
        System.out.println("农民1 decide -> " + (m3 == null ? "null（过牌）" : m3.cards().toString())
                + "  (应 null)");

        System.out.println("=== 4. 回归 100 局 ===");
        int finished = 0, exception = 0, landlordWin = 0;
        long steps = 0;
        int maxStep = 0;
        for (int r = 0; r < 100; r++) {
            GameState st = GameState.start();
            st.bid(0, 3);
            st.settleLandlord();
            int n = 0;
            try {
                while (st.getPhase() == GameState.Phase.PLAYING && n < 800) {
                    int seat = st.getCurrent();
                    Move mv = ai.decide(st, seat);
                    if (mv == null) TurnManager.pass(st, seat);
                    else TurnManager.play(st, seat, mv);
                    n++;
                }
            } catch (Exception e) {
                exception++;
                System.out.println("异常: " + e);
            }
            if (st.getPhase() == GameState.Phase.FINISHED) {
                finished++;
                if (st.getWinner() == st.getLandlord()) landlordWin++;
            } else {
                System.out.println("未结束 第" + r + "局 steps=" + n
                        + " 手牌=" + st.getHand(0).size() + "/" + st.getHand(1).size() + "/" + st.getHand(2).size());
            }
            steps += n;
            maxStep = Math.max(maxStep, n);
        }
        System.out.println("结束 " + finished + "/100，异常 " + exception
                + "，平均 " + (steps / 100.0) + " 步，最长 " + maxStep);
        System.out.println("地主胜率 " + landlordWin + "/100");
    }
}
