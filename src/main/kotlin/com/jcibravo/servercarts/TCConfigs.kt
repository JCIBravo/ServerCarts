package com.jcibravo.servercarts

import com.bergerkiller.bukkit.tc.attachments.control.CartAttachmentSeat
import com.bergerkiller.bukkit.tc.controller.MinecartGroup
import com.bergerkiller.bukkit.tc.controller.MinecartGroupStore
import com.bergerkiller.bukkit.tc.controller.MinecartMember
import com.jcibravo.servercarts.data.CartData
import com.jcibravo.servercarts.data.Speed
import com.jcibravo.servercarts.data.TrainData
import kotlin.math.roundToInt

class TCConfigs {
    fun getCart(uuid: String): CartData? {
        val finalUUID = if (uuid.contains("-")) {
            uuid
        } else {
            uuid.replace(
                "(\\p{XDigit}{8})(\\p{XDigit}{4})(\\p{XDigit}{4})(\\p{XDigit}{4})(\\p{XDigit}{12})".toRegex(),
                "$1-$2-$3-$4-$5"
            )
        }

        return runCatching{ getCarts().filter { it.uuid == finalUUID }[0] }.getOrNull()
    }

    fun getCarts(): List<CartData> {
        val carts = mutableListOf<CartData>()
        for (train in MinecartGroupStore.getGroups()) {
            for (carriage in train) {
                val passengers = getNumberOfPassengers(carriage)
                val seats = getNumberOfSeats(carriage)

                carts.add(
                    CartData(
                        parentTrain = carriage.group.properties.trainName,
                        uuid = carriage.properties.uuid.toString(),
                        entityUUID = carriage.entity.uniqueId.toString(),
                        destination = carriage.properties.destination,
                        passengers = carriage.entity.playerPassengers.filterNotNull().size,
                        passengerNames = carriage.entity.playerPassengers.filterNotNull().map { it.name }.toTypedArray(),
                        speed = getCartSpeed(carriage),
                        remainingCarSeats = seats - passengers,
                        totalCarSeats = seats,
                    )
                )
            }
        }

        return carts
    }

    fun getTrain(name: String): TrainData? {
        val trainMatch = runCatching { MinecartGroupStore.matchAll(name).toList()[0] }.getOrNull()
        if (trainMatch != null) {
            val carriages = mutableListOf<CartData>()
            for (carriage in trainMatch) {
                val passengers = getNumberOfPassengers(carriage)
                val seats = getNumberOfSeats(carriage)

                carriages.add(
                    CartData(
                        parentTrain = carriage.group.properties.trainName,
                        uuid = carriage.properties.uuid.toString(),
                        entityUUID = carriage.entity.uniqueId.toString(),
                        destination = carriage.properties.destination,
                        passengers = carriage.entity.playerPassengers.filterNotNull().size,
                        passengerNames = carriage.entity.playerPassengers.filterNotNull().map { it.name }.toTypedArray(),
                        speed = getCartSpeed(carriage),
                        remainingCarSeats = seats - passengers,
                        totalCarSeats = seats,
                    )
                )
            }

            return TrainData(
                id = trainMatch.properties.trainName,
                name = trainMatch.properties.displayName,
                destination = trainMatch.properties.destination,
                route = trainMatch.properties.destinationRoute,
                nextDestinationRoute = trainMatch.properties.nextDestinationOnRoute,
                tags = getFilteredTags(trainMatch),
                comment = getComments(trainMatch),
                nextStop = getNextStop(trainMatch),
                estimatedTimeOfArrival = getETA(trainMatch),
                isMoving = trainMatch.isMovingOrWaiting,
                passengers = getNumberOfPassengers(trainMatch),
                carriages = trainMatch.size,
                world = trainMatch.properties.location.world,
                x = trainMatch.properties.location.x,
                y = trainMatch.properties.location.y,
                z = trainMatch.properties.location.z,
                trainWagons = carriages,
                speed = getTrainRealSpeed(trainMatch),
                speedLimit = getSpeedLimit(trainMatch),
            )
        } else return null
    }

    fun getTrains(): List<TrainData> {
        val trains = mutableListOf<TrainData>()
        val tcTrains = MinecartGroupStore.getGroups()
        for (train in tcTrains) {
            val carriages = mutableListOf<CartData>()
            for (carriage in train) {
                val passengers = getNumberOfPassengers(carriage)
                val seats = getNumberOfSeats(carriage)

                carriages.add(
                    CartData(
                        parentTrain = carriage.group.properties.trainName,
                        uuid = carriage.properties.uuid.toString(),
                        entityUUID = carriage.entity.uniqueId.toString(),
                        destination = carriage.properties.destination,
                        passengers = carriage.entity.playerPassengers.filterNotNull().size,
                        passengerNames = carriage.entity.playerPassengers.filterNotNull().map { it.name }.toTypedArray(),
                        speed = getCartSpeed(carriage),
                        remainingCarSeats = seats - passengers,
                        totalCarSeats = seats,
                    )
                )
            }

            trains.add(
                TrainData(
                    id = train.properties.trainName,
                    name = train.properties.displayName,
                    destination = train.properties.destination,
                    route = train.properties.destinationRoute,
                    nextDestinationRoute = train.properties.nextDestinationOnRoute,
                    tags = getFilteredTags(train),
                    comment = getComments(train),
                    nextStop = getNextStop(train),
                    estimatedTimeOfArrival = getETA(train),
                    isMoving = train.isMovingOrWaiting,
                    passengers = getNumberOfPassengers(train),
                    carriages = train.size,
                    world = train.properties.location.world,
                    x = train.properties.location.x,
                    y = train.properties.location.y,
                    z = train.properties.location.z,
                    trainWagons = carriages,
                    speed = getTrainRealSpeed(train),
                    speedLimit = getSpeedLimit(train),
                )
            )
        }

        return trains
    }

    fun getNumberOfPassengers(train: MinecartGroup): Int {
        var total = 0
        return try {
            for (carriage in train) { total += carriage.entity.playerPassengers.filterNotNull().size }
            total
        } catch (_: Exception) { 0 }
    }

    fun getNumberOfPassengers(carriage: MinecartMember<*>): Int {
        return carriage.entity.playerPassengers.filterNotNull().size
    }

    fun getComments(train: MinecartGroup): String? {
        val filteredList = train.properties.tags
            .filter { it.startsWith("comment@", true) }

        return runCatching {
            filteredList
                .joinToString(" ") { tag ->
                    tag.replace("comment@", "")
                        .replace(Regex("(?<!\\\\)_"), " ")
                        .replace("\\_", "_")
                }
        }.getOrNull()
    }

    fun getNextStop(train: MinecartGroup): String? {
        val filteredList = train.properties.tags
            .filter { it.startsWith("next@", true) }

        return runCatching {
            filteredList[0]
                .replace("next@", "")
                .replace(Regex("(?<!\\\\)_"), " ")
                .replace("\\_", "_")
        }.getOrNull()
    }

    fun getETA(train: MinecartGroup): Int? {
        val filteredList = train.properties.tags
            .filter { it.startsWith("eta@", true) }

        return runCatching {
            filteredList[0]
                .replace("eta@", "")
                .toInt()
        }.getOrNull()
    }

    fun getNumberOfSeats(carriage: MinecartMember<*>): Int {
        return carriage.attachments.allAttachments.filterIsInstance<CartAttachmentSeat>().size
    }

    fun getFilteredTags(train: MinecartGroup): Array<String> {
        return train.properties.tags
            .filter { !it.startsWith("comment@") && !it.startsWith("eta@") && !it.startsWith("next@") }
            .toTypedArray()
    }

    /**
     * Convert blocks per tick (b/t) to kilometers per hour (Km/h)
     * @param speed Speed in blocks per tick
     * @return Decimal value of the speed in kilometers per hour
     **/
    fun convertToKmh(speed: Double): Double {
        return (speed * 72.00).roundToInt().toDouble()
    }

    /**
     * Convert blocks per tick (b/t) to miles per hour (mph)
     * @param speed Speed in blocks per tick
     * @return Decimal value of the speed in miles per hour
     **/
    fun convertToMph(speed: Double): Double {
        return (speed * 44.74).roundToInt().toDouble()
    }

    fun getCartSpeed(carriage: MinecartMember<*>): Speed {
        return Speed(
            carriage.realSpeedLimited,
            convertToKmh(carriage.realSpeedLimited),
            convertToMph(carriage.realSpeedLimited),
        )
    }

    fun getTrainRealSpeed(train: MinecartGroup): Speed {
        return Speed(
            train[0].realSpeedLimited,
            convertToKmh(train[0].realSpeedLimited),
            convertToMph(train[0].realSpeedLimited),
        )
    }

    fun getSpeedLimit(train: MinecartGroup): Speed {
        return Speed(
            train.properties.speedLimit,
            convertToKmh(train.properties.speedLimit),
            convertToMph(train.properties.speedLimit),
        )
    }
}