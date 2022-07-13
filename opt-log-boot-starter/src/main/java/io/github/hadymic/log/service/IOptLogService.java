package io.github.hadymic.log.service;

import io.github.hadymic.log.model.OptLogRecord;

/**
 * 接收日志
 *
 * @author Hadymic
 */
public interface IOptLogService {

    void record(OptLogRecord optLogRecord);
}
