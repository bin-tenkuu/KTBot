package my.ktbot.utils

import kotlinx.coroutines.delay
import kotlinx.coroutines.runBlocking
import oshi.SystemInfo
import oshi.hardware.*
import oshi.software.os.FileSystem
import oshi.software.os.OSFileStore
import oshi.software.os.OperatingSystem
import oshi.util.FormatUtil.formatBytes
import oshi.util.FormatUtil.formatBytesDecimal
import java.util.*

object SystemInfoUtil {
	val systemInfo: SystemInfo get() = SystemInfo()

	@JvmStatic
	fun main(vararg args: String) {
		val si = systemInfo
		val hal: HardwareAbstractionLayer = si.hardware
		val os: OperatingSystem = si.operatingSystem

		println(os)

		println("Checking computer system...")
		hal.computerSystem.printComputerSystem()

		println("Checking Processor...")
		hal.processor.printProcessor()

		println("Checking Memory...")
		hal.memory.printMemory()

		println("Checking CPU...")
		printCpu(hal.processor)

		println("Checking Sensors...")
		hal.sensors.printSensors()

		println("Checking Disks...")
		printDisks(hal.diskStores)

		println("Checking File System...")
		printFileSystem(os.fileSystem)

	}

	private fun ComputerSystem.printComputerSystem() {
		run {
			println("manufacturer: $manufacturer")
			println("model: $model")
			println("serialnumber: $serialNumber")
		}
		firmware.run {
			println("firmware:")
			println("  manufacturer: $manufacturer")
			println("  name: $name")
			println("  description: $description")
			println("  version: $version")
		}
		baseboard.run {
			println("baseboard:")
			println("  manufacturer: $manufacturer")
			println("  model: $model")
			println("  version: $version")
			println("  serialnumber: $serialNumber")
		}
	}

	private fun CentralProcessor.printProcessor() {
		println(this)
		println(" $physicalPackageCount physical CPU package(s)")
		println(" $physicalProcessorCount physical CPU core(s)")
		println(" $logicalProcessorCount logical CPU(s)")
		println("Identifier: $processorIdentifier")
		println("ProcessorID: ${processorIdentifier.processorID}")
	}

	private fun GlobalMemory.printMemory() {
		println("Memory: ${formatBytes(available)}/${formatBytes(total)}")
		println(
			"Swap used: ${formatBytes(virtualMemory.swapUsed)}/${formatBytes(virtualMemory.swapTotal)}"
		)
	}

	private fun printCpu(processor: CentralProcessor) {
		println("Context Switches/Interrupts: ${processor.contextSwitches} / ${processor.interrupts}")
		val prevTicks = processor.systemCpuLoadTicks
		// Wait a second...
		runBlocking {
			delay(1000)
		}
		val ticks = processor.systemCpuLoadTicks
		val pcpu = ticks.zip(prevTicks) { l, r -> l - r }
		val totalCpu = pcpu.sum()
		System.out.format(
			"User: %.1f%% Nice: %.1f%% System: %.1f%% Idle: %.1f%% IOwait: %.1f%% IRQ: %.1f%% SoftIRQ: %.1f%% Steal: %.1f%%%n",
			*pcpu.map { 100.0 * it / totalCpu }.toTypedArray()
		)
		System.out.format(
			"CPU load: %.1f%% (counting ticks)%n", processor.getSystemCpuLoadBetweenTicks(prevTicks) * 100
		)
	}

	private fun Sensors.printSensors() {
		println("Sensors:")
		System.out.format(" CPU Temperature: %.1f°C%n", cpuTemperature)
		println(" Fan Speeds: ${Arrays.toString(fanSpeeds)}")
		System.out.format(" CPU Voltage: %.1fV%n", cpuVoltage)
	}

	private fun printDisks(list: List<HWDiskStore>) {
		println("Disks:")
		for (disk: HWDiskStore in list) {
			print(" ${disk.name}: (model: ${disk.model} - S/N: ${disk.serial}) size: ")
			print(if (disk.size > 0) formatBytesDecimal(disk.size) else "?")
			print(", ")
			if (disk.reads > 0 || disk.writes > 0) println(
				"reads: ${disk.reads} (${
					formatBytes(disk.readBytes)
				}), writes: ${disk.writes} (${
					formatBytes(disk.writeBytes)
				}), xfer: ${disk.transferTime} ms"
			)
			else println("reads: ? (?), writes: ? (?), xfer: ? ms")
			// TODO Remove when all OS's implemented
			for (part: HWPartition in disk.partitions ?: continue) {
				println(
					" |-- ${part.identification}: ${part.name} (${part.type}) " +
						"Maj:Min=${part.major}:${part.minor}, " +
						"size: ${formatBytesDecimal(part.size)}" +
						if (part.mountPoint.isEmpty()) "" else " @ ${part.mountPoint}"
				)
			}
		}
	}

	private fun printFileSystem(fileSystem: FileSystem) {
		println("File System:")
		println(" File Descriptors: ${fileSystem.openFileDescriptors}/${fileSystem.maxFileDescriptors}")
		for (fs: OSFileStore in fileSystem.fileStores) {
			val usable = fs.usableSpace
			val total = fs.totalSpace
			println(
				" ${fs.name} (${fs.description.ifEmpty { "file system" }}) [${fs.type}] " +
					"${formatBytes(usable)} of ${formatBytes(fs.totalSpace)} " +
					"free (${String.format("%.1f", 100.0 * usable / total)}%) " +
					"mounted at ${fs.mount}"
			)
		}
	}
}
