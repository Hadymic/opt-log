package io.github.hadymic.log.example;

import io.github.hadymic.log.model.OptLogRecord;
import io.github.hadymic.log.service.IOptLogService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.concurrent.ConcurrentHashMap;

/**
 * @author Hadymic
 */
@Slf4j
@Service
public class OptLogTestService implements IOptLogService {

    private final ConcurrentHashMap<String, OptLogRecord> map = new ConcurrentHashMap<>();

    @Override
    public void record(OptLogRecord record) {
        map.put(record.getTenant(), record);
    }

    public OptLogRecord getRecord(String key) {
        return map.get(key);
    }
}
