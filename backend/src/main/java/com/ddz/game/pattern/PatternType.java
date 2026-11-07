package com.ddz.game.pattern;

/**
 * 斗地主全部 14 种合法牌型。
 * 名字里的 WITH 表示"带牌"，例如 TRIPLE_WITH_SINGLE = 三张带一张单牌。
 * 排列顺序大致按牌力从弱到强，方便阅读。
 */
public enum PatternType {

    /** 单张 **/
    SINGLE,

    /** 对子 **/
    PAIR,

    /** 三张 **/
    TRIPLE,

    /** 三带一 **/
    TRIPLE_WITH_SINGLE,

    /** 三带二 **/
    TRIPLE_WITH_PAIR,

    /** 顺子 **/
    STRAIGHT,

    /** 连对 **/
    DOUBLE_STRAIGHT,

    /** 飞机 **/
    AIRPLANE,

    /** 飞机带单 **/
    AIRPLANE_WITH_SINGLE,

    /** 飞机带对 **/
    AIRPLANE_WITH_PAIR,

    /** 四带二（两单）**/
    FOUR_WITH_TWO_SINGLES,

    /** 四带二（两对）**/
    FOUR_WITH_TWO_PAIRS,

    /** 炸弹 **/
    BOMB,

    /** 王炸 **/
    ROCKET
}
