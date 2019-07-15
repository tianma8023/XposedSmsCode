package com.tianma.xsmscode.feature.migrate;

/**
 * 数据迁移过渡接口（版本更新不免遇到旧数据被弃用之类的，兼容旧版本的话就需要进行数据迁移）
 */
public interface ITransition {

    /**
     * 是否需要迁移过渡数据
     */
    boolean shouldTransit();

    /**
     * 执行数据迁移兼容过渡逻辑
     * @return 是否执行成功,是则返回true
     */
    boolean doTransition();

}
