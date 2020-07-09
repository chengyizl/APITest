package pojo;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

import java.util.Map;

@Data
public class LimaTrainline {

    //火车票下单
    @JsonProperty("bookers")
    private String bookers;
    @JsonProperty("contactName")
    private String contactName;
    @JsonProperty("contactTel")
    private String contactTel;
    @JsonProperty("date")
    private String date;
    @JsonProperty("endTime")
    private String endTime;
    @JsonProperty("from")
    private String from;
    @JsonProperty("memberId")
    private String memberId;
    @JsonProperty("runTimeDays")
    private String runTimeDays;
    @JsonProperty("runTimeHour")
    private String runTimeHour;
    @JsonProperty("runTimeMinutes")
    private String runTimeMinutes;
    @JsonProperty("startTime")
    private String startTime;
    @JsonProperty("tenantId")
    private String tenantId;
    @JsonProperty("to")
    private String to;
    @JsonProperty("trainNumber")
    private String trainNumber;
}
