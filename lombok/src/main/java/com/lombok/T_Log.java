package com.lombok;

/**
 * @Log：根据不同的注解生成不同类型的log对象，但是实例名称都是log，有六种可选实现类
 * @CommonsLog Creates log =
 *                     org.apache.commons.logging.LogFactory.getLog(LogExample.class);
 * @Log Creates log =
 *              java.util.logging.Logger.getLogger(LogExample.class.getName());
 * @Log4j Creates log = org.apache.log4j.Logger.getLogger(LogExample.class);
 * @Log4j2 Creates log =
 *                 org.apache.logging.log4j.LogManager.getLogger(LogExample.class);
 * @Slf4j Creates log = org.slf4j.LoggerFactory.getLogger(LogExample.class);
 * @XSlf4j Creates log =
 *                 org.slf4j.ext.XLoggerFactory.getXLogger(LogExample.class);
 * @author hui.zhao
 *
 */
public class T_Log {

}
