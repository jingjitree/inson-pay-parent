package top.inson.springboot.notify.service;

import java.util.Map;

public interface IEPayNotifyService {

    String notifyMe(Map<String, Object> notifyMap, String efpsSign);

}
