<?php

include 'secret.php';

function getConnection()
{
    global $mysql_host, $mysql_user, $mysql_pass, $mysql_db, $mysql_port;

	$conn = new mysqli($mysql_host, $mysql_user, $mysql_pass, $mysql_db, $mysql_port);
	// Check connection
	if ($conn->connect_error) {
		die("Connection failed: " . $conn->connect_error);
	}
	return $conn;
}
?>
