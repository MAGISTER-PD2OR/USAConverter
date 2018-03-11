<link rel="stylesheet" href="./bootstrap/css/bootstrap.min.css" type="text/css">
<link rel="stylesheet" href="kf2stats.css" type="text/css">
<script src="https://ajax.googleapis.com/ajax/libs/jquery/1.12.0/jquery.min.js"></script>
<script src="http://maxcdn.bootstrapcdn.com/bootstrap/3.3.6/js/bootstrap.min.js"></script>
<?php
	include 'database.php';
	include 'traits.php';
	include 'stats.php';

	error_reporting(E_ALL);
	ini_set('display_errors', TRUE);
	ini_set('display_startup_errors', TRUE);

	$conn = getConnection();
	$id = $conn->real_escape_string($_GET['id']);

	/*$name = "";
	$avatar = "";
	$stmt = $conn->prepare("SELECT playername, avatar FROM kf2stats WHERE uid = ?");
	$stmt->bind_param("s",$id);
	if($stmt->execute()) {
		$stmt->bind_result($name,$avatar);
		$stmt->fetch();
		$name = htmlspecialchars($name);
		$stmt->close();
	}*/

	$sql = "SELECT lastperk, exp, kills, playtime, playername, avatar FROM kf2stats WHERE uid=$id";
	$result = $conn->query($sql);
	$result = $result->fetch_assoc();
	$name = htmlspecialchars($result['playername']);

	$sql = "SELECT perkname, statname, value FROM perkstats WHERE uid=$id";
	$stats = $conn->query($sql);

	$sql = "SELECT perkname, traitname, value FROM perktraits WHERE uid=$id";
	$traits = $conn->query($sql);


	$href = "href=\"http://steamcommunity.com/profiles/" . $id . "\"";
?>
	<html>
	<head>
	</head>
	<body>
        <div class="container-fluid">
            <div class="col-sm-offset-1 col-md-offset-2 col-xs-offset-1 col-lg-offset-2 col-sm-10 col-md-8 col-xs-10 col-lg-8">
                <div class="shadow">
                    <h1><a <?php echo $href; ?>><img src=<?php echo "\"" . $result['avatar'] . "\""; ?>><?php echo $name; ?></a></h1>
                    <table class="table">
                        <tr>
                            <td><h3 style="margin-left: 10px">Perk</h3></td>
                            <td><h3>Stat</h3></td>
                            <td><h3>Level</h3></td>
                        </tr>

                        <?php
                        if ($stats->num_rows > 0) {
                            // output data of each row

                            while($row = $stats->fetch_assoc()) {
                                $perkname = substr($row['perkname'],0,-1);
                                $statname = substr($row['statname'],0,-1);
                                $val = min($row['value'],$statMax[$statname]);
                                echo "<tr><td><img class=perkimg src=\"./images/" . $perkname . ".png\"></td>";
                                //echo "<td></td>";
                                echo "<td>" . $statname . "</td>";
                                echo "<td>" . $val . "/" . $statMax[$statname] . "</td></tr>";
                            }
                        }
                        ?>
                        <tr><td colspan="3" class="space"/></tr>
                        <tr>
                            <td><h3 style="margin-left: 10px">Perk</h3></td>
                            <td><h3>Trait</h3></td>
                            <td><h3>Level</h3></td>
                        </tr>

                        <?php
                        if ($traits->num_rows > 0) {
                            // output data of each row
                            while($row = $traits->fetch_assoc()) {
                                $perkname = substr($row['perkname'],0,-1);
                                $traitname = substr($row['traitname'],0,-1);
                                $val = min($row['value'],$traitMax[$traitname]['level']);
                                echo "<tr><td><img class=perkimg src=\"./images/" . $perkname . ".png\"></td>";
                                //echo "<td></td>";
                                //echo "<td><a class=\"tooltip\" href=\"javascript:;\">" . $traitMax[$traitname]['name'] . "<span>" . $traitMax[$traitname]['desc'] . "</span></a></td>";
                                echo "<td><a data-toggle=\"tooltip\" data-html=\"true\" data-placement=\"right\" title=\"" . $traitMax[$traitname]['desc'] .  "\">" . $traitMax[$traitname]['name'] . "</a></td>";
                                
                                //echo "<td>" . $traitMax[$traitname]['name'] . "</td>";
                                echo "<td>" . $val . "/" . $traitMax[$traitname]['level'] . "</td></tr>";
                            }
                        } 
                        ?>
                    </table>
                </div>
            </div>
        </div>
        <script>
            $(document).ready(function(){
                $('[data-toggle="tooltip"]').tooltip();
            });
        </script>
	</body>
</html>

<?php
	$conn->close();
?>