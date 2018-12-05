INSERT INTO report(test_id, test_number, test_name, test_script, test_host, test_host_role, test_result,
    test_result_message, location, aggregated)
    VALUES(1, 1, 'unit-test', 'unknown', 'sender.domain', 'sender', 'success', '', '/not/exist/1/s', false);
INSERT INTO report(test_id, test_number, test_name, test_script, test_host, test_host_role, test_result,
    test_result_message, location, aggregated)
    VALUES(1, 1, 'unit-test', 'unknown', 'receiver.domain', 'receiver', 'success',  '', '/not/exist/1/r', false);
INSERT INTO report(test_id, test_number, test_name, test_script, test_host, test_host_role, test_result,
    test_result_message, location, aggregated)
    VALUES(1, 1, 'unit-test', 'unknown', 'inspector.domain', 'inspector', 'success',  '', '/not/exist/1/i', false);
INSERT INTO report(test_id, test_number, test_name, test_script, test_host, test_host_role, test_result,
    test_result_message, location, aggregated)
    VALUES(1, 1, 'unit-test', 'unknown', null, null, 'success',  '', '/not/exist/1/a', true);
INSERT INTO report(test_id, test_number, test_name, test_script, test_host, test_host_role, test_result,
    test_result_message, location, aggregated)
    VALUES(2, 1, 'unit-test', 'unknown', 'sender.domain', 'sender', 'success', '', '/not/exist/2/s', false);
INSERT INTO report(test_id, test_number, test_name, test_script, test_host, test_host_role, test_result,
    test_result_message, location, aggregated)
    VALUES(2, 1, 'unit-test', 'unknown', 'receiver.domain', 'receiver', 'success', '', '/not/exist/2/r', false);
INSERT INTO report(test_id, test_number, test_name, test_script, test_host, test_host_role, test_result,
    test_result_message, location, aggregated)
    VALUES(2, 1, 'unit-test', 'unknown', 'inspector.domain', 'inspector', 'success', '', '/not/exist/2/i', false);
INSERT INTO report(test_id, test_number, test_name, test_script, test_host, test_host_role, test_result,
    test_result_message, location, aggregated)
    VALUES(2, 1, 'unit-test', 'unknown', null, null, 'success', '', '/not/exist/2/a', true);