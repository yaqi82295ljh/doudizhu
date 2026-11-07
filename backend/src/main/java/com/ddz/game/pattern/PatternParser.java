package com.ddz.game.pattern;

import com.ddz.game.card.Card;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public class PatternParser {

    public static Optional<CardPattern> parse(List<Card> cards) {

        if (cards == null || cards.isEmpty()) {
            return Optional.empty();
        }

        /*建表，下标对应点数，cnt[i]对应张数*/
        int[] cnt = new int[18];
        for (Card card : cards) {
            cnt[card.rank()]++;
        }

        /*王炸：小王、大王各一张，两种点数，所以要写在下面的判断之前*/
        if (cards.size() == 2 && cnt[Card.JOKER_SMALL] == 1 && cnt[Card.JOKER_BIG] == 1) {
            return Optional.of(new CardPattern(PatternType.ROCKET, Card.JOKER_BIG, 2));
        }

        /*扫表，记下有几种不同的点数，那些点数具体是谁*/
        List<Integer> theRanks = new ArrayList<>();
        for (int i = 3; i < cnt.length; i++) {
            if (cnt[i] != 0) {
                theRanks.add(i);
            }
        }

        //点数只有一个时的所有牌型
        if (theRanks.size() == 1) {
            int mainRank = theRanks.get(0);      // 唯一那个点数
            int qty = cnt[mainRank];             // 它有几张牌

            switch (qty) {
                case 1:
                    return Optional.of(new CardPattern(PatternType.SINGLE, mainRank, qty));
                case 2:
                    return Optional.of(new CardPattern(PatternType.PAIR, mainRank, qty));
                case 3:
                    return Optional.of(new CardPattern(PatternType.TRIPLE, mainRank, qty));
                case 4:
                    return Optional.of(new CardPattern(PatternType.BOMB, mainRank, qty));
                default:
                    return Optional.empty();
            }
        }

        //纯飞机不带牌
        if (theRanks.size() >= 2 && cards.size() >= 6) {
            boolean isFeiJi = true;

            for (int i = 1; i < theRanks.size(); i++) {
                if (theRanks.get(i)-theRanks.get(i - 1) != 1) {
                    isFeiJi = false;
                    break;
                }
            }

            if (theRanks.get(theRanks.size() - 1) > 14) {
                isFeiJi = false;
            }


            for (int i = 0; i < theRanks.size(); i++) {
                if (cnt[theRanks.get(i)] != 3) {
                    isFeiJi = false;
                    break;
                }
            }
            if (isFeiJi) {
                int maxRank = theRanks.get(theRanks.size() - 1);
                int total = cards.size();
                return Optional.of(new CardPattern(PatternType.AIRPLANE,maxRank , total));
            }

        }

        //飞机带牌
        if (cards.size() >= 8) {
            List<Integer> triples = new ArrayList<>();
            for (int r : theRanks) {
                if (cnt[r] == 3) {
                    triples.add(r);
                }
            }
            if (triples.size() >= 2) {                 // 先保证至少有 2 组，顺便避免空 List
                boolean isFeiJi = true;

                for (int i = 1; i < triples.size(); i++) {
                    if (triples.get(i) - triples.get(i - 1) != 1) {
                        isFeiJi = false;
                        break;
                    }
                }

                if (triples.get(triples.size() - 1) > 14) {
                    isFeiJi = false;
                }

                int n = triples.size();
                if (isFeiJi && cards.size() == 4 * n) {
                    return Optional.of(new CardPattern(
                            PatternType.AIRPLANE_WITH_SINGLE, triples.get(n - 1), cards.size()));
                }

                //飞机带对：翅膀必须是 n 个对子
                if (isFeiJi && cards.size() == 5 * n) {
                    int pairCount = 0;
                    boolean wingsOk = true;
                    for (int r : theRanks) {
                        if (cnt[r] != 3) {
                            if (cnt[r] != 2) {
                                wingsOk = false;
                                break;
                            }
                            pairCount++;
                        }
                    }
                    if (wingsOk && pairCount == n) {
                        return Optional.of(new CardPattern(
                                PatternType.AIRPLANE_WITH_PAIR, triples.get(n - 1), cards.size()));
                    }
                }
            }
        }


        //四带二
        if (cards.size() == 6 || cards.size() == 8) {
            int fourRank = -1;
            for (int r : theRanks) {
                if (cnt[r] == 4) {
                    fourRank = r;
                    break;
                }
            }

            if (fourRank != -1) {
                if (cards.size() == 6) {
                    return Optional.of(new CardPattern(
                            PatternType.FOUR_WITH_TWO_SINGLES, fourRank, 6));
                }

                boolean wingsOk = true;
                for (int r : theRanks) {
                    if (r != fourRank && cnt[r] != 2) {
                        wingsOk = false;
                        break;
                    }
                }
                if (wingsOk) {
                    return Optional.of(new CardPattern(
                            PatternType.FOUR_WITH_TWO_PAIRS, fourRank, 8));
                }
            }
        }



        //点数有两种时的所有牌型
        if (theRanks.size() == 2) {

            int mainRank1 = theRanks.get(0);
            int mainRank2 = theRanks.get(1);
            int total = cnt[mainRank1] + cnt[mainRank2];

            switch (total) {
                case 4:
                    if (cnt[mainRank1] != cnt[mainRank2]) {
                        //三带一
                        if (cnt[mainRank1] == 3) {
                            return Optional.of(new CardPattern(PatternType.TRIPLE_WITH_SINGLE, mainRank1, 4));
                        } else {
                            return Optional.of(new CardPattern(PatternType.TRIPLE_WITH_SINGLE, mainRank2, 4));
                        }
                    }
                        return Optional.empty();
                case 5:
                    if(Math.abs(cnt[mainRank1] - cnt[mainRank2]) == 1) {
                        //三带对
                        if (cnt[mainRank1] == 3) {
                            return Optional.of(new CardPattern(PatternType.TRIPLE_WITH_PAIR, mainRank1, 5));
                        }else{
                            return Optional.of(new CardPattern(PatternType.TRIPLE_WITH_PAIR, mainRank2, 5));
                        }
                    }
                        return Optional.empty();
                default:
                    return Optional.empty();
            }

        }

        //顺子
        if (theRanks.size() >= 5 && theRanks.size() <= 12 && cards.size() >= 5) {
            //点数连续，最大A,每个点数只有一张
            boolean isShunZi = true;
            for (int i = 1; i < theRanks.size(); i++) {
                if (theRanks.get(i)-theRanks.get(i - 1) != 1) {
                    isShunZi = false;
                    break;
                }
            }
            for (int i = 0; i < theRanks.size(); i++) {
                if (cnt[theRanks.get(i)] != 1) {
                    isShunZi = false;
                    break;
                }
            }
            if (theRanks.get(theRanks.size()-1) > 14) {
                isShunZi = false;
            }

            if (isShunZi) {
                int maxRank = theRanks.get(theRanks.size()-1);
                int total = cards.size();
                return Optional.of(new CardPattern(PatternType.STRAIGHT, maxRank, total));
            }

        }

        //连对
        if (theRanks.size() >= 3 && theRanks.size() <= 12 && cards.size() >= 6) {
            boolean isLianDui = true;

            for (int i = 1; i < theRanks.size(); i++) {
                if (theRanks.get(i)-theRanks.get(i - 1) != 1) {
                    isLianDui = false;
                    break;
                }
            }

            if(theRanks.get(theRanks.size()-1) > 14){
                isLianDui = false;
            }

            for (int i = 0; i < theRanks.size(); i++) {
                if (cnt[theRanks.get(i)] != 2) {
                    isLianDui = false;
                }
            }
            if (isLianDui) {
                int maxRank = theRanks.get(theRanks.size()-1);
                int total = cards.size();
                return Optional.of(new CardPattern(PatternType.DOUBLE_STRAIGHT, maxRank, total));
            }

        }

        return Optional.empty();
    }
}
