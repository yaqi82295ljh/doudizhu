package com.ddz.game.move;

import com.ddz.game.card.Card;
import com.ddz.game.pattern.CardPattern;
import com.ddz.game.pattern.PatternComparator;
import com.ddz.game.pattern.PatternParser;
import com.ddz.game.pattern.PatternType;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

class MoveGeneratorTest {

    private static List<Card> cards(String... ids) {
        List<Card> list = new ArrayList<>();
        for (String id : ids) {
            list.add(Card.of(id));
        }
        return list;
    }

    private static CardPattern last(String... ids) {
        return PatternParser.parse(cards(ids)).orElseThrow(
                () -> new AssertionError("上家的牌本身就不合法: " + String.join(" ", ids)));
    }

    private static int size(List<Move> moves) {
        return moves.size();
    }

    /** 所有候选必须自洽：牌数和牌型 count 一致，且确实能压过 last */
    private static void assertAllConsistent(List<Move> moves, CardPattern last) {
        for (Move m : moves) {
            assertEquals(m.pattern().count(), m.cards().size(),
                    "候选的牌数和牌型 count 不一致: " + m.pattern().type());
            CardPattern reparsed = PatternParser.parse(m.cards()).orElseThrow(
                    () -> new AssertionError("候选牌型本身解析失败"));
            assertEquals(reparsed.type(), m.pattern().type(), "候选牌型与重新解析结果不一致");
            assertTrue(PatternComparator.canBeat(m.pattern(), last), "候选其实压不过 last");
        }
    }

    // ==================== 跟牌 ====================

    @Test
    void 压单张_只出单张_点数更大() {
        // 手牌 333 5 77 大王；4 大于 3，所以 5、7、大王 都能压
        List<Move> moves = MoveGenerator.beatingMoves(
                cards("S3", "H3", "C3", "D5", "S7", "H7", "JOKER_B"), last("S4"));
        assertEquals(3, size(moves));
        assertAllConsistent(moves, last("S4"));
    }

    @Test
    void 压对子_三张可以拆出对子但点数要更大() {
        // 对 3 压不住对 4；只有对 7 可以
        List<Move> moves = MoveGenerator.beatingMoves(
                cards("S3", "H3", "C3", "D5", "S7", "H7", "JOKER_B"), last("S4", "H4"));
        assertEquals(1, size(moves));
        assertAllConsistent(moves, last("S4", "H4"));
    }

    @Test
    void 压三张_点数不够就压不了() {
        List<Move> moves = MoveGenerator.beatingMoves(
                cards("S3", "H3", "C3", "D5", "S7", "H7", "JOKER_B"), last("S4", "H4", "C4"));
        assertEquals(0, size(moves));
    }

    @Test
    void 压顺子_长度必须相同() {
        List<Move> moves = MoveGenerator.beatingMoves(
                cards("S3", "H4", "C5", "D6", "S7", "H8", "C9", "D10"),
                last("S3", "H4", "C5", "D6", "S7"));
        // 45678、56789、6789 10 共 3 条
        assertEquals(3, size(moves));
        assertAllConsistent(moves, last("S3", "H4", "C5", "D6", "S7"));
    }

    @Test
    void 压顺子_含2的段不会被枚举() {
        List<Move> moves = MoveGenerator.beatingMoves(
                cards("SQ", "HK", "CA", "S2"), last("S3", "H4", "C5", "D6", "S7"));
        assertEquals(0, size(moves));
    }

    @Test
    void 压连对_长度必须相同() {
        List<Move> moves = MoveGenerator.beatingMoves(
                cards("S3", "H3", "C4", "D4", "S5", "H5", "C6", "D6", "S7", "H7"),
                last("S3", "H3", "C4", "D4", "S5", "H5"));
        // 44 55 66、55 66 77 共 2 条
        assertEquals(2, size(moves));
    }

    @Test
    void 压三带一_翅膀逐个枚举() {
        List<Move> moves = MoveGenerator.beatingMoves(
                cards("S5", "H5", "C5", "D7", "S8", "JOKER_B"),
                last("S3", "H3", "C3", "D2"));
        // 555+7、555+8、555+大王
        assertEquals(3, size(moves));
        assertAllConsistent(moves, last("S3", "H3", "C3", "D2"));
    }

    @Test
    void 压三带二() {
        List<Move> moves = MoveGenerator.beatingMoves(
                cards("S5", "H5", "C5", "D7", "H7", "S8", "H8"),
                last("S3", "H3", "C3", "D2", "H2"));
        assertEquals(2, size(moves));
    }

    @Test
    void 压纯飞机() {
        List<Move> moves = MoveGenerator.beatingMoves(
                cards("S3", "H3", "C3", "S4", "H4", "C4", "S5", "H5", "C5"),
                last("S3", "H3", "C3", "S4", "H4", "C4"));
        assertEquals(1, size(moves));
        assertEquals(PatternType.AIRPLANE, moves.get(0).pattern().type());
    }

    @Test
    void 压飞机带单() {
        List<Move> moves = MoveGenerator.beatingMoves(
                cards("S4", "H4", "C4", "S5", "H5", "C5", "D7", "S8"),
                last("S3", "H3", "C3", "S4", "H4", "C4", "D5", "S6"));
        assertEquals(1, size(moves));
        assertEquals(PatternType.AIRPLANE_WITH_SINGLE, moves.get(0).pattern().type());
    }

    @Test
    void 压飞机带对() {
        List<Move> moves = MoveGenerator.beatingMoves(
                cards("S4", "H4", "C4", "S5", "H5", "C5", "D7", "H7", "S8", "H8"),
                last("S3", "H3", "C3", "S4", "H4", "C4", "D5", "H5", "S6", "H6"));
        assertEquals(1, size(moves));
        assertEquals(PatternType.AIRPLANE_WITH_PAIR, moves.get(0).pattern().type());
    }

    @Test
    void 压四带二_炸弹也会一起被列出() {
        List<Move> moves = MoveGenerator.beatingMoves(
                cards("S4", "H4", "C4", "D4", "S7", "H8"),
                last("S3", "H3", "C3", "D3", "D5", "S6"));
        // 四带二(4 带 7、8) + 炸弹 4444
        assertEquals(2, size(moves));
        assertAllConsistent(moves, last("S3", "H3", "C3", "D3", "D5", "S6"));
    }

    @Test
    void 王炸能压炸弹() {
        List<Move> moves = MoveGenerator.beatingMoves(
                cards("JOKER_S", "JOKER_B", "S3"),
                last("S4", "H4", "C4", "D4"));
        assertEquals(1, size(moves));
        assertEquals(PatternType.ROCKET, moves.get(0).pattern().type());
    }

    // ==================== 首出 ====================

    @Test
    void 首出枚举_手牌333_5_77_大王() {
        List<Move> moves = MoveGenerator.allMoves(
                cards("S3", "H3", "C3", "D5", "S7", "H7", "JOKER_B"));
        // 单张 4 + 对子 2 + 三张 1 + 三带一 3 + 三带二 1 = 11
        assertEquals(11, size(moves));
        assertAllConsistent(moves, null);
    }

    @Test
    void 首出枚举_没有重复候选() {
        List<Move> moves = MoveGenerator.allMoves(
                cards("S3", "H3", "C3", "D5", "S7", "H7", "JOKER_B"));
        Set<String> seen = new HashSet<>();
        for (Move m : moves) {
            List<String> ids = m.cards().stream().map(Card::id).sorted().toList();
            assertTrue(seen.add(ids.toString()), "出现重复候选: " + ids);
        }
    }

    @Test
    void 首出枚举_空手牌返回空() {
        assertEquals(0, MoveGenerator.allMoves(new ArrayList<>()).size());
    }
}
