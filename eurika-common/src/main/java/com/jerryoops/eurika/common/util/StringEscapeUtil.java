package com.jerryoops.eurika.common.util;

import cn.hutool.core.lang.Filter;
import cn.hutool.core.util.EscapeUtil;

public class StringEscapeUtil {

    private static Filter<Character> hashKeyFilter;
    private static Filter<Character> slashFilter;

    static {
        hashKeyFilter = c -> c == '#';
        slashFilter = c -> c == '/';
    }

    public static String escapeHashKey(String text) {
        return EscapeUtil.escape(text, hashKeyFilter);
    }

    public static String escapeSlash(String text) {
        return EscapeUtil.escape(text, slashFilter);
    }

    public static String escape(String text, char... escapeChars) {
        return EscapeUtil.escape(text, character -> {
            for (char c : escapeChars) {
                // 仅对escapeChars中的字符进行转义
                if (c == character) return true;
            }
            return false;
        });
    }

    public static String unescape(String text) {
        return EscapeUtil.unescape(text);
    }
}
