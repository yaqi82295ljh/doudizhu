package com.ddz.game.pattern;

import com.ddz.game.card.Card;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class PatternComparatorTest {

    /** 把牌面 id 解析成 CardPattern，用来当 canBeat 的两个参数 */
    private static CardPattern p(String... ids) {
        List<Card> list = new ArrayList<>();
        for (String id : ids) {
            list.add(Card.of(id));
        }
        return PatternParser.parse(list).orElseThrow(
                () -> new AssertionError("这手牌本身就不合法: " + String.join(" ", ids)));
    }

    @Test
    void 首出时任何牌都能出() {
        assertTrue(PatternComparator.canBeat(p("S3"), null));
    }

    @Test
    void 同类型比点数更大才能压() {
        assertTrue(PatternComparator.canBeat(p("S5", "H5"), p("S3", "H3")));
        assertFalse(PatternComparator.canBeat(p("S3", "H3"), p("S5", "H5")));
    }

    @Test
    void 点数相同不能压() {
        assertFalse(PatternComparator.canBeat(p("S5", "H5"), p("C5", "D5")));
    }

    @Test
    void 类型不同不能压() {
        assertFalse(PatternComparator.canBeat(p("S3", "H3"), p("S4")));
    }

    @Test
    void 顺子点数更大可以压() {
        assertTrue(PatternComparator.canBeat(
                p("S4", "H5", "C6", "D7", "S8"),
                p("S3", "H4", "C5", "D6", "S7")));
    }

    @Test
    void 顺子张数不同不能压() {
        // 7 张顺子点数更大，但长度不同，规则上不能压 5 张顺子
        assertFalse(PatternComparator.canBeat(
                p("S3", "H4", "C5", "D6", "S7", "H8", "C9"),
                p("S4", "H5", "C6", "D7", "S8")));
    }

    @Test
    void 炸弹能压任何非炸弹() {
        assertTrue(PatternComparator.canBeat(
                p("S3", "H3", "C3", "D3"),
                p("S10", "HJ", "CQ", "DK", "SA")));
    }

    @Test
    void 炸弹之间比点数() {
        assertTrue(PatternComparator.canBeat(
                p("S4", "H4", "C4", "D4"),
                p("S3", "H3", "C3", "D3")));
        // 2222 的 2 比 3 大，3333 压不住
        assertFalse(PatternComparator.canBeat(
                p("S3", "H3", "C3", "D3"),
                p("S2", "H2", "C2", "D2")));
    }

    @Test
    void 王炸压炸弹() {
        assertTrue(PatternComparator.canBeat(
                p("JOKER_S", "JOKER_B"),
                p("S3", "H3", "C3", "D3")));
    }

    @Test
    void 炸弹压不过王炸() {
        assertFalse(PatternComparator.canBeat(
                p("S3", "H3", "C3", "D3"),
                p("JOKER_S", "JOKER_B")));
    }

    @Test
    void 王炸压不过王炸() {
        assertFalse(PatternComparator.canBeat(
                p("JOKER_S", "JOKER_B"),
                p("JOKER_S", "JOKER_B")));
    }

    @Test
    void 三带一只比三张的点数_带的牌不影响() {
        // 555 带大王  vs  333 带 2：主牌 5 > 3，能压
        assertTrue(PatternComparator.canBeat(
                p("S5", "H5", "C5", "JOKER_B"),
                p("S3", "H3", "C3", "D2")));
    }

    @Test
    void 单张2能压A() {
        assertTrue(PatternComparator.canBeat(p("S2"), p("SA")));
    }
}
