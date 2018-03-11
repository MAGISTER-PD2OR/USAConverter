<link rel="stylesheet" href="./bootstrap/css/bootstrap.min.css" type="text/css">
<link rel="stylesheet" href="kf2stats.css" type="text/css">
<?php

include 'database.php';

$page = $_GET['page'];
$count = $_GET['count'];

$conn = getConnection();

$sql ="SELECT COUNT(*) AS total FROM kf2stats";
$data = $conn->query($sql);
$db_count = $data->fetch_assoc();
$db_count = $db_count['total'];

$sql = "SELECT uid, exp, kills, playtime, playername, avatar FROM kf2stats ORDER BY kills DESC LIMIT " . $page * $count . ", " . $count;
$result = $conn->query($sql);
?>

<html>
	<head>
	</head>
	<body>
        <div class="container-fluid">
            <div class="col-sm-offset-1 col-md-offset-2 col-xs-offset-1 col-lg-offset-2 col-sm-10 col-md-8 col-xs-10 col-lg-8">
                <div class="shadow">
                    <h1>Hall of Fame</h1>
                    <table class="table">
                        <tr>
                            <td><h3 style="margin-left: 10px">Player</h3></td>
                            <td><h3>Experience</h3></td>
                            <td><h3>Kills</h3></td>
                            <td><h3>Playtime</h3></td>
                        </tr>

                        <?php
                            if ($result->num_rows > 0) {
                                while($row = $result->fetch_assoc()) {
                                    $playername = htmlspecialchars($row['playername']);
                                    $href = "href=\"./kf2playerstats.php?id=" . $row['uid'] . "\"";
                                    $seconds = $row['playtime'];

                                    echo "<tr><td class=\"playername\"><a $href><img src=\"" . $row['avatar'] . "\">" . $playername . "</a></td>";
                                    echo "<td>" . number_format($row['exp'], 0, ',', '.') . "</td>";
                                    echo "<td>" . number_format($row['kills'], 0, ',', '.') . "</td>";
                                    echo "<td>" . sprintf("%.1f hours", $seconds/60/60) . "</td></tr>";
                                }
                            }
                        ?>

                        <tr><td colspan="4" align=center>
                        <?php 
                            if($page > 0) {
                                $href = "href=\"./kf2stats.php?page=" . ($page-1) . "&count=" . $count . "\"";
                                echo "<a $href>previous</a>";
                            }
                            if($page > 0 && $page*$count+$count < $db_count) {
                                echo "&nbsp&nbsp&nbsp&nbsp&nbsp";
                            }
                            if($page*$count+$count < $db_count) {
                                $href = "href=\"./kf2stats.php?page=" . ($page+1) . "&count=" . $count . "\"";
                                echo "<a $href>next</a>";
                            }
                        ?>
                        </td></tr>
                    </table>
                </div>
            </div>
        </div>
	</body>
</html>

<?php
$conn->close();
?>