package com.github.guang19.leaf.core;

import com.github.guang19.leaf.core.common.Result;

/**
 * @author guang19
 * @date 2020/8/16
 * @description  Id Generator
 */
public interface IdGenerator
{
    public abstract Result nextId();
}
