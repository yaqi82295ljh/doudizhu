package com.ddz.game.pattern;

import com.ddz.game.card.Card;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * PatternParser 的回归测试。
 * 方法名就是需求描述，挂了看名字就知道哪条规则被改坏了。
 */
class PatternParserTest {

    /** 用牌面 id 快速造一手牌，例如 cards("S3", "H3", "C3") */
    private static List<Card> cards(String... ids) {
        List<Card> list = new ArrayList<>();
        for (String id : ids) {
            list.add(Card.of(id));
        }
        return list;
    }

    /** 断言这手牌被识别成指定牌型，并校验主牌点数与总张数 */
    private static void assertPattern(PatternType type, int mainRank, int count, List<Card> hand) {
        CardPattern p = PatternParser.parse(hand).orElseThrow(
                () -> new AssertionError("期望识别为 " + type + "，实际返回空"));
        assertEquals(type, p.type(), "牌型不对");
        assertEquals(mainRank, p.mainRank(), "主牌点数不对");
        assertEquals(count, p.count(), "总张数不对");
    }

    /** 断言这手牌不是任何合法牌型 */
    private static void assertIllegal(List<Card> hand) {
        Optional<CardPattern> p = PatternParser.parse(hand);
        assertTrue(p.isEmpty(), "期望判为非法，实际识别成了 " + p.map(CardPattern::type).orElse(null));
    }

    // ==================== 一、点数唯一的牌型 ====================

    @Test
    void 单张() {
        assertPattern(PatternType.SINGLE, 3, 1, cards("S3"));
    }

    @Test
    void 单张大王() {
        assertPattern(PatternType.SINGLE, 17, 1, cards("JOKER_B"));
    }

    @Test
    void 对子() {
        assertPattern(PatternType.PAIR, 3, 2, cards("S3", "H3"));
    }

    @Test
    void 三张() {
        assertPattern(PatternType.TRIPLE, 3, 3, cards("S3", "H3", "C3"));
    }

    @Test
    void 炸弹() {
        assertPattern(PatternType.BOMB, 4, 4, cards("S4", "H4", "C4", "D4"));
    }

    @Test
    void 王炸() {
        assertPattern(PatternType.ROCKET, 17, 2, cards("JOKER_S", "JOKER_B"));
    }

    @Test
    void 两张不同点数不是牌型() {
        assertIllegal(cards("S3", "H4"));
    }

    // ==================== 二、两种点数：三带一 / 三带二 ====================

    @Test
    void 三带一_主牌取三张的点数() {
        // 333 + 5，比大小比的是 3，不是带的 5
        assertPattern(PatternType.TRIPLE_WITH_SINGLE, 3, 4, cards("S3", "H3", "C3", "D5"));
    }

    @Test
    void 三带一_三张是大的那个点数时主牌跟着变() {
        // 555 + 3，主牌必须是 5
        assertPattern(PatternType.TRIPLE_WITH_SINGLE, 5, 4, cards("S5", "H5", "C5", "D3"));
    }

    @Test
    void 三带一_带大王也改变不了主牌() {
        assertPattern(PatternType.TRIPLE_WITH_SINGLE, 3, 4, cards("S3", "H3", "C3", "JOKER_B"));
    }

    @Test
    void 三带二() {
        assertPattern(PatternType.TRIPLE_WITH_PAIR, 3, 5, cards("S3", "H3", "C3", "D5", "H5"));
    }

    @Test
    void 三带二_三张是大的那个点数() {
        assertPattern(PatternType.TRIPLE_WITH_PAIR, 5, 5, cards("S5", "H5", "C5", "D3", "H3"));
    }

    @Test
    void 两个对子一起出不是牌型() {
        assertIllegal(cards("S3", "H3", "D5", "H5"));
    }

    @Test
    void 四张同点加一张不是三带() {
        assertIllegal(cards("S4", "H4", "C4", "D4", "S5"));
    }

    // ==================== 三、顺子 ====================

    @Test
    void 顺子五张() {
        assertPattern(PatternType.STRAIGHT, 7, 5, cards("S3", "H4", "C5", "D6", "S7"));
    }

    @Test
    void 顺子到A() {
        assertPattern(PatternType.STRAIGHT, 14, 5, cards("S10", "HJ", "CQ", "DK", "SA"));
    }

    @Test
    void 顺子八张() {
        assertPattern(PatternType.STRAIGHT, 10, 8,
                cards("S3", "H4", "C5", "D6", "S7", "H8", "C9", "D10"));
    }

    @Test
    void 顺子不能含2() {
        assertIllegal(cards("SJ", "HQ", "CK", "DA", "S2"));
    }

    @Test
    void 顺子不能以A打头接2() {
        assertIllegal(cards("SA", "H2", "C3", "D4", "S5"));
    }

    @Test
    void 顺子四张不成立() {
        assertIllegal(cards("S3", "H4", "C5", "D6"));
    }

    @Test
    void 顺子不能有对子() {
        // 33 4567：点数种类是 3~7 连续，但 3 有两张
        assertIllegal(cards("S3", "H3", "C4", "D5", "S6", "H7"));
    }

    @Test
    void 顺子末尾有对子也不算() {
        assertIllegal(cards("S3", "H4", "C5", "D6", "S7", "H7"));
    }

    @Test
    void 顺子必须连续() {
        assertIllegal(cards("S3", "H4", "C5", "D7", "S8"));
    }

    @Test
    void 顺子不能含王() {
        assertIllegal(cards("S3", "H4", "C5", "D6", "JOKER_S"));
    }

    // ==================== 四、连对 ====================

    @Test
    void 连对三对() {
        assertPattern(PatternType.DOUBLE_STRAIGHT, 5, 6, cards("S3", "H3", "C4", "D4", "S5", "H5"));
    }

    @Test
    void 连对四对() {
        assertPattern(PatternType.DOUBLE_STRAIGHT, 6, 8,
                cards("S3", "H3", "C4", "D4", "S5", "H5", "C6", "D6"));
    }

    @Test
    void 连对两对不成立() {
        assertIllegal(cards("S3", "H3", "C4", "D4"));
    }

    @Test
    void 连对不能含2() {
        assertIllegal(cards("SK", "HK", "CA", "DA", "C2", "D2"));
    }

    @Test
    void 连对里混了单牌就不是连对() {
        assertIllegal(cards("S3", "H3", "C4", "D4", "S5", "H6"));
    }

    // ==================== 五、飞机 ====================

    @Test
    void 纯飞机两组() {
        assertPattern(PatternType.AIRPLANE, 4, 6, cards("S3", "H3", "C3", "S4", "H4", "C4"));
    }

    @Test
    void 纯飞机三组() {
        assertPattern(PatternType.AIRPLANE, 5, 9,
                cards("S3", "H3", "C3", "S4", "H4", "C4", "S5", "H5", "C5"));
    }

    @Test
    void 飞机不能含2() {
        assertIllegal(cards("SA", "HA", "CA", "S2", "H2", "C2"));
    }

    @Test
    void 飞机主体必须连续() {
        assertIllegal(cards("S3", "H3", "C3", "S5", "H5", "C5"));
    }

    @Test
    void 飞机带单() {
        assertPattern(PatternType.AIRPLANE_WITH_SINGLE, 4, 8,
                cards("S3", "H3", "C3", "S4", "H4", "C4", "S5", "H6"));
    }

    @Test
    void 飞机带单_翅膀恰好成对也算带单() {
        // 8 张 = 4 x 2，翅膀 2 张成对，规则上仍算带两单
        assertPattern(PatternType.AIRPLANE_WITH_SINGLE, 4, 8,
                cards("S3", "H3", "C3", "S4", "H4", "C4", "S5", "H5"));
    }

    @Test
    void 飞机带单_三组主体带三张翅膀() {
        assertPattern(PatternType.AIRPLANE_WITH_SINGLE, 5, 12,
                cards("S3", "H3", "C3", "S4", "H4", "C4", "S5", "H5", "C5",
                        "S6", "H7", "C8"));
    }

    @Test
    void 飞机带单_张数不匹配就不算() {
        assertIllegal(cards("S3", "H3", "C3", "S4", "H4", "C4", "S5"));           // 7 张
        assertIllegal(cards("S3", "H3", "C3", "S4", "H4", "C4", "S5", "H6", "C7")); // 9 张
    }

    @Test
    void 飞机带对() {
        assertPattern(PatternType.AIRPLANE_WITH_PAIR, 4, 10,
                cards("S3", "H3", "C3", "S4", "H4", "C4", "S5", "H5", "S6", "H6"));
    }

    @Test
    void 飞机带对_翅膀全是单牌就不算() {
        assertIllegal(cards("S3", "H3", "C3", "S4", "H4", "C4", "S5", "H6", "C7", "D8"));
    }

    @Test
    void 飞机带对_翅膀不能是四张同点() {
        assertIllegal(cards("S3", "H3", "C3", "S4", "H4", "C4", "S5", "H5", "C5", "D5"));
    }

    // ==================== 六、四带二 ====================

    @Test
    void 四带二两单() {
        assertPattern(PatternType.FOUR_WITH_TWO_SINGLES, 3, 6,
                cards("S3", "H3", "C3", "D3", "S5", "H6"));
    }

    @Test
    void 四带二_两张单牌成对也算() {
        assertPattern(PatternType.FOUR_WITH_TWO_SINGLES, 3, 6,
                cards("S3", "H3", "C3", "D3", "S5", "H5"));
    }

    @Test
    void 四带二_可以带王() {
        assertPattern(PatternType.FOUR_WITH_TWO_SINGLES, 3, 6,
                cards("S3", "H3", "C3", "D3", "JOKER_S", "JOKER_B"));
    }

    @Test
    void 四带二两对() {
        assertPattern(PatternType.FOUR_WITH_TWO_PAIRS, 3, 8,
                cards("S3", "H3", "C3", "D3", "S5", "H5", "S6", "H6"));
    }

    @Test
    void 四带二_翅膀是三张加一张就不算() {
        assertIllegal(cards("S4", "H4", "C4", "D4", "S5", "H5", "C5", "S6"));
    }

    @Test
    void 四带二_两个四张不成立() {
        assertIllegal(cards("S3", "H3", "C3", "D3", "S5", "H5", "C5", "D5"));
    }

    @Test
    void 四带二_五张不成立() {
        assertIllegal(cards("S3", "H3", "C3", "D3", "S5"));
    }

    // ==================== 七、边界输入 ====================

    @Test
    void 空列表不是牌型() {
        assertIllegal(new ArrayList<>());
    }

    @Test
    void null不是牌型() {
        assertTrue(PatternParser.parse(null).isEmpty());
    }
}
