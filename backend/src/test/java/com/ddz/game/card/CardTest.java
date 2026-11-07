package com.ddz.game.card;

import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

class CardTest {

    @Test
    void 点数与花色的编号() {
        Card c = Card.of("S3");
        assertEquals(3, c.rank());
        assertEquals(0, c.suit());
    }

    @Test
    void 两位数点数的解析() {
        assertEquals(10, Card.of("H10").rank());
    }

    @Test
    void 大小王没有花色() {
        assertEquals(Card.JOKER_SMALL, Card.of("JOKER_S").rank());
        assertEquals(Card.SUIT_JOKER, Card.of("JOKER_S").suit());
        assertEquals(Card.JOKER_BIG, Card.of("JOKER_B").rank());
    }

    @Test
    void 全副54张牌往返不丢信息() {
        List<Card> deck = Deck.newDeck();
        assertEquals(54, deck.size());
        for (Card c : deck) {
            assertEquals(c, Card.of(c.id()), "往返后对不上: " + c.id());
        }
    }

    @Test
    void 同点数不同花色是两张不同的牌() {
        assertNotEquals(Card.of("S3"), Card.of("H3"));
    }

    @Test
    void 非法花色抛异常() {
        assertThrows(IllegalArgumentException.class, () -> Card.of("X3"));
    }

    @Test
    void 非法点数抛异常() {
        assertThrows(IllegalArgumentException.class, () -> Card.of("S1"));
    }
}
