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
	private val os: OperatingSystem

	/**
	 * 硬件抽象层。提供对处理器、内存、电池和磁盘等硬件项目的访问
	 */
	private val hal: HardwareAbstractionLayer

	init {
		val systemInfo = SystemInfo()
		hal = systemInfo.hardware
		os = systemInfo.operatingSystem
	}

	/**
	 * 硬件：BIOS/固件和主板、逻辑板等组件
	 */
	private val computerSystem: ComputerSystem = hal.computerSystem

	/**
	 * 硬件：CPU
	 */
	private val cpu: CentralProcessor = hal.processor

	/**
	 * 硬件：内存
	 */
	private val memory: GlobalMemory = hal.memory

	/**
	 * 硬件：硬盘或其他类似的存储设备
	 */
	private val diskStores: List<HWDiskStore> = hal.diskStores

	/**
	 * 文件系统
	 */
	private val fileSystem: FileSystem = os.fileSystem

	@JvmStatic
	fun main(vararg args: String) {
		println(SystemInfoUtil())
	}

	operator fun invoke(): String = buildString {
		appendLine(os)

		// println("Checking computer system...")
		printComputerSystem(computerSystem)

		// println("Checking CPU...")
		printProcessor(cpu)

		// println("Checking Memory...")
		printMemory(memory)

		// println("Checking Disks...")
		printDisks(diskStores)

		// println("Checking File System...")
		printFileSystem(fileSystem)
	}

	private fun StringBuilder.printComputerSystem(system: ComputerSystem) {
		appendLine("manufacturer: ${system.manufacturer}")
		system.firmware.run {
			appendLine("firmware:")
			appendLine("  manufacturer: $manufacturer")
			appendLine("  version: $version")
		}
		system.baseboard.run {
			appendLine("baseboard:")
			appendLine("  manufacturer: $manufacturer")
			appendLine("  version: $version")
		}
	}

	private fun StringBuilder.printProcessor(processor: CentralProcessor) {
		appendLine("Identifier: ${processor.processorIdentifier.name}")
		appendLine("  ${processor.physicalPackageCount} physical CPU package(s)")
		appendLine("  ${processor.physicalProcessorCount} physical CPU core(s)")
		appendLine("  ${processor.logicalProcessorCount} logical CPU(s)")
		appendLine("Context Switches/Interrupts: ${processor.contextSwitches} / ${processor.interrupts}")
		val prevTicks = processor.systemCpuLoadTicks
		// Wait a second...
		runBlocking {
			delay(1000)
		}
		appendLine(
			String.format(
				"CPU load: %.1f%% (counting ticks)%n", processor.getSystemCpuLoadBetweenTicks(prevTicks) * 100
			)
		)
	}

	private fun StringBuilder.printMemory(memory: GlobalMemory) {
		appendLine("Memory: ${formatBytes(memory.available)}/${formatBytes(memory.total)}")
		appendLine(
			"Swap used: ${formatBytes(memory.virtualMemory.swapUsed)}/${formatBytes(memory.virtualMemory.swapTotal)}"
		)
	}

	private fun StringBuilder.printDisks(list: List<HWDiskStore>) {
		appendLine("Disks:")
		for (disk: HWDiskStore in list) {
			append(" ${disk.name}: (model: ${disk.model}) size: ")
			append(if (disk.size > 0) formatBytesDecimal(disk.size) else "?")
			append(", ")
			if (disk.reads > 0 || disk.writes > 0) appendLine(
				"reads: ${disk.reads} (${
					formatBytes(disk.readBytes)
				}), writes: ${disk.writes} (${
					formatBytes(disk.writeBytes)
				}), xfer: ${disk.transferTime} ms"
			)
			else appendLine("reads: ? (?), writes: ? (?), xfer: ? ms")
			for (part: HWPartition in disk.partitions ?: continue) {
				appendLine(
					" |- ${part.identification}: ${part.name} (${part.type}) " +
						"Maj:Min=${part.major}:${part.minor}, " +
						"size: ${formatBytesDecimal(part.size)}" +
						if (part.mountPoint.isEmpty()) "" else " @ ${part.mountPoint}"
				)
			}
		}
	}

	private fun StringBuilder.printFileSystem(fileSystem: FileSystem) {
		appendLine("File System:")
		appendLine(" File Descriptors: ${fileSystem.openFileDescriptors}/${fileSystem.maxFileDescriptors}")
		for (fs: OSFileStore in fileSystem.fileStores) {
			val usable = fs.usableSpace
			val total = fs.totalSpace
			appendLine(
				"  ${fs.name} (${fs.description.ifEmpty { "file system" }}) [${fs.type}] " +
					"${formatBytes(usable)} of ${formatBytes(fs.totalSpace)} " +
					"free (${String.format("%.1f", 100.0 * usable / total)}%) " +
					"mounted at ${fs.mount}"
			)
		}
	}
}
