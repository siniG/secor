package com.pinterest.secor.parser;

import java.text.SimpleDateFormat;
import java.util.Date;

import net.minidev.json.JSONObject;
import net.minidev.json.JSONValue;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.pinterest.secor.common.SecorConfig;
import com.pinterest.secor.message.Message;

/**
 * DateMessageParser extracts timestamp field (specified by 'message.timestamp.name') 
 *  and the date pattern (specified by 'message.timestamp.input.pattern')
 *
 * @see http://docs.oracle.com/javase/6/docs/api/java/text/SimpleDateFormat.html
 *
 * @author Lucas Zago (lucaszago@gmail.com)
 *
 */
public class CoralogixMessageParser extends MessageParser {
    private static final Logger LOG = LoggerFactory.getLogger(DateMessageParser.class);
    protected final String defaultFormatter = "yyyy_MM_dd_HH_mm";
    private static final long ROUND_FIFTEEN_MINUTES = 30*60*1000;
    protected static final String defaultCompanyId = "0";
    protected static final String companyIdTag = "companyId";
    protected static final String sdkTag = "sdk";

    private String mFormatter;

    public CoralogixMessageParser(SecorConfig config) {
        super(config);
        mFormatter = mConfig.getString("secor.message.parser.coralogix.formatter",null);
        if(mFormatter == null){
            mFormatter = defaultFormatter;
        }
    }

    @Override
    public String[] extractPartitions(Message message) {
        JSONObject jsonObject = (JSONObject) JSONValue.parse(message.getPayload());

        long timeMs = System.currentTimeMillis();
        long roundedtimeMs = (timeMs/ROUND_FIFTEEN_MINUTES) * ROUND_FIFTEEN_MINUTES;
        Date date = new Date(roundedtimeMs);

        SimpleDateFormat outputDateFormatter = new SimpleDateFormat(mFormatter);
        String strDateFormat = outputDateFormatter.format(date);

        String[] result = { defaultCompanyId, strDateFormat };

        if (jsonObject != null) {
            JSONObject sdkInfo = (JSONObject) jsonObject.get(sdkTag);
            if ( sdkInfo != null) {
                Object companyFieldValue =  sdkInfo.get(companyIdTag);
                if (companyFieldValue != null) {
                    try {
                        result[0] = companyFieldValue.toString();
                        return result;
                    } catch (Exception e) {
                        LOG.warn("Impossible to convert companyId value {} . Using tag = {} . Using default",
                                companyFieldValue.toString(), companyIdTag, result[0]);
                    }
                }
            }
        }
        return result;
    }
}

