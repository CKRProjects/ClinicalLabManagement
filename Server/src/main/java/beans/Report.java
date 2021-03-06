package beans;

import java.util.Date;

public class Report {

    private String reportId;

    private String patientName;

    private String age;

    private String gender;

    private String mobile;

    private String testName;

    private Test test;

    private Date created;

    private Date lastModified;

    private DateObj sampleCollectionDate;

    private DateObj reportingDate;

    private String referredBy;

    private ReportStatus reportStatus;

    public String getReportId() {
        return reportId;
    }

    public void setReportId(String reportId) {
        this.reportId = reportId;
    }

    public String getPatientName() {
        return patientName;
    }

    public void setPatientName(String patientName) {
        this.patientName = patientName;
    }

    public String getAge() {
        return age;
    }

    public void setAge(String age) {
        this.age = age;
    }

    public String getGender() {
        return gender;
    }

    public void setGender(String gender) {
        this.gender = gender;
    }

    public String getMobile() {
        return mobile;
    }

    public void setMobile(String mobile) {
        this.mobile = mobile;
    }

    public String getTestName() {
        return testName;
    }

    public void setTestName(String testName) {
        this.testName = testName;
    }

    public Test getTest() {
        return test;
    }

    public void setTest(Test test) {
        this.test = test;
    }

    public Date getCreated() {
        return created;
    }

    public void setCreated(Date created) {
        this.created = created;
    }

    public Date getLastModified() {
        return lastModified;
    }

    public void setLastModified(Date lastModified) {
        this.lastModified = lastModified;
    }

    public DateObj getSampleCollectionDate() {
        return sampleCollectionDate;
    }

    public void setSampleCollectionDate(DateObj sampleCollectionDate) {
        this.sampleCollectionDate = sampleCollectionDate;
    }

    public DateObj getReportingDate() {
        return reportingDate;
    }

    public void setReportingDate(DateObj reportingDate) {
        this.reportingDate = reportingDate;
    }

    public String getReferredBy() {
        return referredBy;
    }

    public void setReferredBy(String referredBy) {
        this.referredBy = referredBy;
    }

    public ReportStatus getReportStatus() {
        return reportStatus;
    }

    public void setReportStatus(ReportStatus reportStatus) {
        this.reportStatus = reportStatus;
    }
}
