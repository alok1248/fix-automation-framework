@FIX42
@Lifecycle
@Qty5
Feature: FIX42_LIFECYCLE_001 - New Order Ack

  Basic Trading Lifecycle
  Simple NewOrderSingle acknowledgment when OrderQty = 5

  Background:
    Given FIX session is up

  Scenario: New Order Ack for Qty 5
    When client sends NewOrderSingle with qty 5
    Then server should send ExecutionReport Ack with qty 5
