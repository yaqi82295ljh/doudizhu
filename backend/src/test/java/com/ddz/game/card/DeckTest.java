package com.ddz.game.card;

import org.junit.jupiter.api.Test;

import java.util.HashSet;
import java.util.List;
import java.util.Random;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

class DeckTest {

    @Test
    void 一副牌54张且无重复() {
        List<Card> deck = Deck.newDeck();
        assertEquals(54, deck.size());
        assertEquals(54, new HashSet<>(deck).size());
    }

    @Test
    void 大小王各一张() {
        List<Card> deck = Deck.newDeck();
        assertEquals(1, deck.stream().filter(c -> c.rank() == Card.JOKER_SMALL).count());
        assertEquals(1, deck.stream().filter(c -> c.rank() == Card.JOKER_BIG).count());
    }

    @Test
    void 发牌张数是17_17_17_3() {
        List<List<Card>> deal = Deck.deal(new Random(42));
        assertEquals(17, deal.get(0).size());
        assertEquals(17, deal.get(1).size());
        assertEquals(17, deal.get(2).size());
        assertEquals(3, deal.get(3).size());
    }

    @Test
    void 发出去的牌合起来正好54张且无重复() {
        List<List<Card>> deal = Deck.deal(new Random(42));
        HashSet<Card> all = new HashSet<>();
        deal.forEach(all::addAll);
        assertEquals(54, all.size());
    }

    @Test
    void 同一个种子发两次牌结果完全一样() {
        assertEquals(Deck.deal(new Random(42)), Deck.deal(new Random(42)));
    }

    @Test
    void 不同种子发出来的牌不一样() {
        assertTrue(!Deck.deal(new Random(42)).equals(Deck.deal(new Random(99))));
    }

    @Test
    void 拿到的是独立副本改一份不影响另一份() {
        List<List<Card>> deal = Deck.deal(new Random(42));
        int before = deal.get(1).size();
        deal.get(0).remove(0);
        assertEquals(before, deal.get(1).size());
    }
}
