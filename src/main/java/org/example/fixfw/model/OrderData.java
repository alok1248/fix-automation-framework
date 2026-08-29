package org.example.fixfw.model;

public class OrderData {
    private String symbol;
    private char side;
    private int qty;
    private char ordType;
    private char tif;
    private double price;
    private String currency;
    private String securityId;
    private String securityIdSource;
    private String exchange;
    private char handlInst;
    private String account;
    private String senderSubId;
    private String deliverToCompId;

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    private String email;

    public OrderData(){

    }

    public OrderData(String deliverToCompId, String senderSubId, String account, char handlInst, String exchange, String securityIdSource, String securityId, String currency, double price, char tif, char ordType, int qty, char side, String symbol) {
        this.deliverToCompId = deliverToCompId;
        this.senderSubId = senderSubId;
        this.account = account;
        this.handlInst = handlInst;
        this.exchange = exchange;
        this.securityIdSource = securityIdSource;
        this.securityId = securityId;
        this.currency = currency;
        this.price = price;
        this.tif = tif;
        this.ordType = ordType;
        this.qty = qty;
        this.side = side;
        this.symbol = symbol;
    }

    public String getSymbol() {
        return symbol;
    }

    public void setSymbol(String symbol) {
        this.symbol = symbol;
    }

    public char getSide() {
        return side;
    }

    public void setSide(char side) {
        this.side = side;
    }

    public int getQty() {
        return qty;
    }

    public void setQty(int qty) {
        this.qty = qty;
    }

    public char getOrdType() {
        return ordType;
    }

    public void setOrdType(char ordType) {
        this.ordType = ordType;
    }

    public char getTif() {
        return tif;
    }

    public void setTif(char tif) {
        this.tif = tif;
    }

    public double getPrice() {
        return price;
    }

    public void setPrice(double price) {
        this.price = price;
    }

    public String getCurrency() {
        return currency;
    }

    public void setCurrency(String currency) {
        this.currency = currency;
    }

    public String getSecurityId() {
        return securityId;
    }

    public void setSecurityId(String securityId) {
        this.securityId = securityId;
    }

    public String getSecurityIdSource() {
        return securityIdSource;
    }

    public void setSecurityIdSource(String securityIdSource) {
        this.securityIdSource = securityIdSource;
    }

    public String getExchange() {
        return exchange;
    }

    public void setExchange(String exchange) {
        this.exchange = exchange;
    }

    public char getHandlInst() {
        return handlInst;
    }

    public void setHandlInst(char handlInst) {
        this.handlInst = handlInst;
    }

    public String getAccount() {
        return account;
    }

    public void setAccount(String account) {
        this.account = account;
    }

    public String getSenderSubId() {
        return senderSubId;
    }

    public void setSenderSubId(String senderSubId) {
        this.senderSubId = senderSubId;
    }

    public String getDeliverToCompId() {
        return deliverToCompId;
    }

    public void setDeliverToCompId(String deliverToCompId) {
        this.deliverToCompId = deliverToCompId;
    }
}
