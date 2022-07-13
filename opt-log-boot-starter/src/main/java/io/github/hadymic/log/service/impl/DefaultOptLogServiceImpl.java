package io.github.hadymic.log.service.impl;

import io.github.hadymic.log.model.OptLogRecord;
import io.github.hadymic.log.service.IOptLogService;
import lombok.extern.slf4j.Slf4j;

/**
 * 接收日志的默认实现
 *
 * @author Hadymic
 */
@Slf4j
public class DefaultOptLogServiceImpl implements IOptLogService {
    @Override
    public void record(OptLogRecord optLogRecord) {
        log.info("OptLog record: {}", optLogRecord);
    }
}
