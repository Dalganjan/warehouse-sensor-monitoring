Build a fat jar first and run it:
```
./gradlew jar
java -cp build/libs/warehouse-sensor-monitoring.jar com.example.warehouse.MainKt
Note: you'll need to add the application plugin and set mainClass in build.gradle.kts for ./gradlew run to work. Alternatively just use java -cp.
```

Send test sensor data

Once the app is running, simulate sensors using netcat:
```
# Temperature reading (above threshold → triggers alarm)
echo "sensor_id=TEMP-01; value=38.5" | nc -u localhost 3344

# Humidity reading (above threshold → triggers alarm)
echo "sensor_id=HUM-A3; value=55.0" | nc -u localhost 3355

# Below threshold → no alarm
echo "sensor_id=TEMP-02; value=20.0" | nc -u localhost 3344
You should see WARN log lines in the console for any reading that exceeds the thresholds (35°C for temperature, 50% for humidity).
```

Screenshot of Sample executed results are added under results dir. Please check.

Thanks,
Dalganjan Sengar
