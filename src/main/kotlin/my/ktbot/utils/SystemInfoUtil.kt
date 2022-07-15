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

object SystemInfoUtil {
	/**
	 * 操作系统
	 */
	val os: OperatingSystem

	/**
	 * 硬件抽象层。提供对处理器、内存、电池和磁盘等硬件项目的访问
	 */
	val hal: HardwareAbstractionLayer

	init {
		val systemInfo = SystemInfo()
		hal = systemInfo.hardware
		os = systemInfo.operatingSystem
	}

	/**
	 * 硬件：BIOS/固件和主板、逻辑板等组件
	 */
	val computerSystem: ComputerSystem = hal.computerSystem

	/**
	 * 硬件：CPU
	 */
	val cpu: CentralProcessor = hal.processor

	/**
	 * 硬件：内存
	 */
	val memory: GlobalMemory = hal.memory

	/**
	 * 硬件：硬盘或其他类似的存储设备
	 */
	val diskStores: List<HWDiskStore> = hal.diskStores

	/**
	 * 文件系统
	 */
	val fileSystem: FileSystem = os.fileSystem

	@JvmStatic
	fun main(vararg args: String) {
		println(os)

		println("Checking computer system...")
		computerSystem.printComputerSystem()

		println("Checking CPU...")
		cpu.printProcessor()

		println("Checking Memory...")
		memory.printMemory()

		println("Checking Disks...")
		printDisks(diskStores)

		println("Checking File System...")
		printFileSystem(fileSystem)
	}

	private fun ComputerSystem.printComputerSystem() {
		println("manufacturer: $manufacturer")
		firmware.run {
			println("firmware:")
			println("  manufacturer: $manufacturer")
			println("  version: $version")
		}
		baseboard.run {
			println("baseboard:")
			println("  manufacturer: $manufacturer")
			println("  version: $version")
		}
	}

	private fun CentralProcessor.printProcessor() {
		println("Identifier: ${processorIdentifier.name}")
		println("  $physicalPackageCount physical CPU package(s)")
		println("  $physicalProcessorCount physical CPU core(s)")
		println("  $logicalProcessorCount logical CPU(s)")
		println("Context Switches/Interrupts: $contextSwitches / $interrupts")
		val prevTicks = systemCpuLoadTicks
		// Wait a second...
		runBlocking {
			delay(1000)
		}
		System.out.format("CPU load: %.1f%% (counting ticks)%n", getSystemCpuLoadBetweenTicks(prevTicks) * 100)
	}

	private fun GlobalMemory.printMemory() {
		println("Memory: ${formatBytes(available)}/${formatBytes(total)}")
		println("Swap used: ${formatBytes(virtualMemory.swapUsed)}/${formatBytes(virtualMemory.swapTotal)}")
	}

	private fun printDisks(list: List<HWDiskStore>) {
		println("Disks:")
		for (disk: HWDiskStore in list) {
			print(" ${disk.name}: (model: ${disk.model}) size: ")
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
			for (part: HWPartition in disk.partitions ?: continue) {
				println(
					" |- ${part.identification}: ${part.name} (${part.type}) " +
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
				"  ${fs.name} (${fs.description.ifEmpty { "file system" }}) [${fs.type}] " +
					"${formatBytes(usable)} of ${formatBytes(fs.totalSpace)} " +
					"free (${String.format("%.1f", 100.0 * usable / total)}%) " +
					"mounted at ${fs.mount}"
			)
		}
	}
}
