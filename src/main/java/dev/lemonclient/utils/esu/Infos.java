package dev.lemonclient.utils.esu;

public class Infos {
    public static class WBInfo {
        String status;
        String message;
        String id;
        String phone;
        String phonediqu;

        public WBInfo(String status, String message, String id, String phone, String phonediqu) {
            this.status = status;
            this.message = message;
            this.id = id;
            this.phone = phone;
            this.phonediqu = phonediqu;
        }

        public String getStatus() {
            return this.status;
        }

        public void setStatus(String status) {
            this.status = status;
        }

        public String getMessage() {
            return this.message;
        }

        public void setMessage(String message) {
            this.message = message;
        }

        public String getID() {
            return this.id;
        }

        public void setId(String id) {
            this.id = id;
        }

        public String getPhone() {
            return this.phone;
        }

        public void setPhone(String phone) {
            this.phone = phone;
        }

        public String getPhonediqu() {
            return this.phonediqu;
        }

        public void setPhonediqu(String phonediqu) {
            this.phonediqu = phonediqu;
        }
    }

    public static class SfzInfo {
        String success;
        Result result;

        public SfzInfo(String success, Result result) {
            this.success = success;
            this.result = result;
        }

        public String getSuccess() {
            return this.success;
        }

        public void setSuccess(String success) {
            this.success = success;
        }

        public Result getResult() {
            return this.result;
        }

        public void setResult(Result result) {
            this.result = result;
        }
    }

    public static class Result {
        String status;
        String par;
        String idcard;
        String born;
        String sex;
        String att;
        String postno;
        String areano;
        String style_simcall;
        String style_citynm;
        String msg;

        public Result(String status, String idcard, String par, String born, String sex, String att, String postno, String areano, String style_simcall, String style_citynm, String msg) {
            this.status = status;
            this.idcard = idcard;
            this.par = par;
            this.born = born;
            this.sex = sex;
            this.att = att;
            this.postno = postno;
            this.areano = areano;
            this.style_simcall = style_simcall;
            this.style_citynm = style_citynm;
            this.msg = msg;
        }

        public String getStatus() {
            return this.status;
        }

        public void setStatus(String status) {
            this.status = status;
        }

        public String getIdcard() {
            return this.idcard;
        }

        public void setIdcard(String idcard) {
            this.idcard = idcard;
        }

        public String getPar() {
            return this.par;
        }

        public void setPar(String par) {
            this.par = par;
        }

        public String getBorn() {
            return this.born;
        }

        public void setBorn(String born) {
            this.born = born;
        }

        public String getSex() {
            return this.sex;
        }

        public void setSex(String sex) {
            this.sex = sex;
        }

        public String getAtt() {
            return this.att;
        }

        public void setAtt(String att) {
            this.att = att;
        }

        public String getPostno() {
            return this.postno;
        }

        public void setPostno(String postno) {
            this.postno = postno;
        }

        public String getAreano() {
            return this.areano;
        }

        public void setAreano(String areano) {
            this.areano = areano;
        }

        public String getStyle_simcall() {
            return this.style_simcall;
        }

        public void setStyle_simcall(String style_simcall) {
            this.style_simcall = style_simcall;
        }

        public String getStyle_citynm() {
            return this.style_citynm;
        }

        public void setStyle_citynm(String style_citynm) {
            this.style_citynm = style_citynm;
        }

        public String getMsg() {
            return this.msg;
        }

        public void setMsg(String msg) {
            this.msg = msg;
        }

    }

    public static class QQlmInfo {
        String status;
        String message;
        String qq;
        String qqlm;

        public QQlmInfo(String status, String message, String qq, String qqlm) {
            this.status = status;
            this.message = message;
            this.qq = qq;
            this.qqlm = qqlm;
        }

        public String getStatus() {
            return this.status;
        }

        public void setStatus(String status) {
            this.status = status;
        }

        public String getMessage() {
            return this.message;
        }

        public void setMessage(String message) {
            this.message = message;
        }

        public String getQq() {
            return this.qq;
        }

        public void setQq(String qq) {
            this.qq = qq;
        }

        public String getQqlm() {
            return this.qqlm;
        }

        public void setQqlm(String qqlm) {
            this.qqlm = qqlm;
        }
    }

    public static class QQInfo {
        String status;
        String message;
        String qq;
        String phone;
        String phonediqu;

        public QQInfo(String status, String message, String qq, String phone, String phonediqu) {
            this.status = status;
            this.message = message;
            this.qq = qq;
            this.phone = phone;
            this.phonediqu = phonediqu;
        }

        public String getStatus() {
            return this.status;
        }

        public void setStatus(String status) {
            this.status = status;
        }

        public String getMessage() {
            return this.message;
        }

        public void setMessage(String message) {
            this.message = message;
        }

        public String getQq() {
            return this.qq;
        }

        public void setQq(String qq) {
            this.qq = qq;
        }

        public String getPhone() {
            return this.phone;
        }

        public void setPhone(String phone) {
            this.phone = phone;
        }

        public String getPhonediqu() {
            return this.phonediqu;
        }

        public void setPhonediqu(String phonediqu) {
            this.phonediqu = phonediqu;
        }
    }

    public static class LOLInfo {
        String status;
        String message;
        String qq;
        String name;
        String daqu;

        public LOLInfo(String status, String message, String qq, String name, String daqu) {
            this.status = status;
            this.message = message;
            this.qq = qq;
            this.name = name;
            this.daqu = daqu;
        }

        public String getStatus() {
            return this.status;
        }

        public void setStatus(String status) {
            this.status = status;
        }

        public String getMessage() {
            return this.message;
        }

        public void setMessage(String message) {
            this.message = message;
        }

        public String getQq() {
            return this.qq;
        }

        public void setQq(String qq) {
            this.qq = qq;
        }

        public String getName() {
            return this.name;
        }

        public void setName(String name) {
            this.name = name;
        }

        public String getDaqu() {
            return this.daqu;
        }

        public void setDaqu(String daqu) {
            this.daqu = daqu;
        }
    }
}
