@FIX42
@Lifecycle
@Qty10
Feature: FIX42_LIFECYCLE_002 - New Order Reject

  Basic Trading Lifecycle
  NewOrderSingle rejection when OrderQty = 10

  Background:
    Given FIX session is up

  Scenario: New Order Reject for Qty 10
    When client sends NewOrderSingle with qty 10
    Then server should send ExecutionReport Reject with qty 10
