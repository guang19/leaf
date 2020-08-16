package com.github.guang19.leaf.core.common;

import lombok.*;

/**
 * @author guang19  , meituan leaf
 * @date 2020/8/16
 * @description  Result
 */
@Setter
@Getter
@ToString
@NoArgsConstructor
@AllArgsConstructor
public class Result
{
    private long id;

    private String message;

    private Status status;

    //系统回拨
    public static Result systemClockGoBack()
    {
        return new Result(-1,"Current server system clock go back !", Status.EXCEPTION);
    }

    //正常
    public static Result ok(long id)
    {
        return new Result(id,null,Status.SUCCESS);
    }
}
