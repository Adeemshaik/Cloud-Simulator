import java.io.File
import java.util
import java.util.{ArrayList, List}

import com.ADCS441.cloudsim.Base.{CloudletBase, DataCenterBase}
import com.ADCS441.cloudsim.Simulations.Simulation_1.{cloudlet, config, datacenterbase}
import com.typesafe.config.{Config, ConfigFactory}
import org.cloudbus.cloudsim.allocationpolicies.{VmAllocationPolicy, VmAllocationPolicyBestFit, VmAllocationPolicyFirstFit, VmAllocationPolicySimple}
import org.cloudbus.cloudsim.cloudlets.CloudletSimple
import org.cloudbus.cloudsim.core.CloudSim
import org.cloudbus.cloudsim.datacenters.DatacenterSimple
import org.cloudbus.cloudsim.hosts.{Host, HostSimple}
import org.cloudbus.cloudsim.resources.{Pe, PeSimple}
import org.cloudbus.cloudsim.schedulers.cloudlet.CloudletSchedulerTimeShared
import org.cloudbus.cloudsim.schedulers.vm.VmSchedulerTimeShared
import org.cloudbus.cloudsim.utilizationmodels.UtilizationModelDynamic
import org.cloudbus.cloudsim.vms.{Vm, VmSimple}
import org.junit.Assert
import org.junit.Test

import scala.collection.JavaConverters._

class TestClass {
  val Test_Variable = 1
  val simulation = new CloudSim
  //val config : Config = ConfigFactory.load("src/test/resources/application_test.conf")
  val config =  ConfigFactory.parseFile(new File("src/test/resources/application_test.conf"))
  val orig_config = ConfigFactory.parseFile(new File("src/main/resources/application.conf"))
  val test_Variable = config.getInt("Test_Variable")
  val cloud_service_provider = config.getConfigList("CloudCompanies")
  val HOST_TYPE: Config = cloud_service_provider.get(0).getConfig("HostType")

  val SERVICE_PROVIDER = cloud_service_provider.get(0).getConfig("ServiceProvider")
  val DATACENTERS1 = cloud_service_provider.get(0).getConfig("datacenters1")
  val DATACENTER_TYPE = cloud_service_provider.get(0).getConfig("DataCenterType")
  val VMAllocationPolicy = cloud_service_provider.get(0).getConfig("vm_allocation_policy")
  val CLOUDLETS = cloud_service_provider.get(0).getConfig("Cloudlets")
  val NO_OF_CLOUDLETS = CLOUDLETS.getInt("CLOUDLETS_NUM")
  val NO_OF_PES_PER_CLOUDLET = CLOUDLETS.getInt("CLOUDLET_PES")
  val LENGTH_OF_CLOUDLET = CLOUDLETS.getInt("CLOUDLET_LENGTH")
  val CLOUDLETS_CPU_UTIL = cloud_service_provider.get(0).getConfig("UtilizationModel")
  val VM_ARCH = cloud_service_provider.get(0).getConfig("vm_arch")
  val VMS1 = cloud_service_provider.get(0).getConfig("vm1")
  val CLOUDLETS_TYPE =  orig_config.getConfigList("CloudCompanies").get(0).getConfig("CloudletType").getString("cloudlet")


  val HOSTS_NO = orig_config.getConfigList("CloudCompanies").get(0).getInt("no_of_hosts")
  @Test
  def checkConfig(): Unit ={
    Assert.assertEquals(config.getInt("Test_Variable"),Test_Variable)
  }

  @Test
  def checkAllocationPolicy():Unit={
    val vmp = new VmAllocationPolicyFirstFit()
    val policy = new DataCenterBase().getVMAllocatioPolicy("FirstFit")
    Assert.assertNotNull(policy)
    Assert.assertEquals(vmp.getClass,policy.getClass)
  }

  @Test
  def checkDataCenterCreation():Unit ={

    val mips = config.getConfig("test_host").getLong("mips")
    val pes = config.getConfig("test_host").getInt("no_pes")

    val peList = new util.ArrayList[Pe]
    peList.add(new PeSimple(mips))

    val ram = config.getConfig("test_host").getLong("ram") // in Megabytes
    val storage = config.getConfig("test_host").getLong("storage")
    val bw = config.getConfig("test_host").getLong("bandwidth") //in Megabits/s

    val hostList = (1 to HOSTS_NO).map{
      x => new HostSimple(ram, bw, storage, peList)
    }.toList

    val datacentertest = new DatacenterSimple(simulation, hostList.asJava, new VmAllocationPolicyFirstFit)
    val datacenter = new DataCenterBase().createDataCenter(HOSTS_NO,SERVICE_PROVIDER.getString("datacenter1"),DATACENTER_TYPE.getString("Simple"),VMAllocationPolicy.getString("First_fit"),simulation,HOST_TYPE.getString("host1"))

    Assert.assertNotNull(datacenter)
    Assert.assertEquals(datacenter.getClass,datacentertest.getClass)
    Assert.assertEquals(datacenter.getHostList.size(),datacentertest.getHostList.size())
  }
  @Test
  def checkVMListCreation():Unit={
    val VM_NO = cloud_service_provider.get(0).getInt("no_of_vms")
    //val VMS = cloud_service_provider.get(0).getConfig("Vms")
    val VM_TYPE = cloud_service_provider.get(0).getConfig("Vm_Type")
    val CLOUDLETS_SCHEDULER = cloud_service_provider.get(0).getConfig("Cloudlet_Scheduler")

    val VM_ARCH = orig_config.getConfigList("CloudCompanies").get(0).getConfig("vm_arch")
    val VMS1 = orig_config.getConfigList("CloudCompanies").get(0).getConfig("vm1")

    val mips = VMS1.getInt("mips")
    val size = VMS1.getInt("size")
    val ram = VMS1.getInt("ram")
    val bw = VMS1.getInt("bandwidth")
    val VM_PES = VMS1.getInt("vm_pes")
    //val vm1 = new VmSimple(mips, VM_PES).setRam(ram).setBw(bw).setSize(size).setCloudletScheduler(new CloudletSchedulerTimeShared)
    val vmListTest = (1 to VM_NO).map {
      vm =>
        new VmSimple(mips, VM_PES).setRam(ram).setBw(bw).setSize(size).setCloudletScheduler(new CloudletSchedulerTimeShared)
    }.toList
    //vmlist.add(vm1)
    val vmList = new DataCenterBase().createVmList(VM_NO,VM_TYPE.getString("Simple"),CLOUDLETS_SCHEDULER.getString("TimeShared"),VM_ARCH.getString("vm1"))
    Assert.assertEquals(vmListTest.size,vmList.size)
    Assert.assertEquals(vmListTest(0).getCloudletScheduler.getClass,vmList(0).getCloudletScheduler.getClass)
    Assert.assertEquals(vmListTest(0).getRam.getCapacity,vmList(0).getRam.getCapacity)
  }

  @Test
  def checkCloudletCreation():Unit ={

    val cloudletListTest =(1 to NO_OF_CLOUDLETS).map{
      cd =>
        new CloudletSimple(LENGTH_OF_CLOUDLET,NO_OF_PES_PER_CLOUDLET).setUtilizationModelCpu(new UtilizationModelDynamic(0.5))
    }.toList
    val cloudletList = new CloudletBase().Cloudlets(NO_OF_CLOUDLETS,CLOUDLETS_TYPE,CLOUDLETS_CPU_UTIL.getDouble("half"))
    Assert.assertNotNull(cloudletList)
    Assert.assertEquals(cloudletListTest.getClass, cloudletList.getClass)
    Assert.assertEquals(cloudletList(0).getUtilizationOfCpu(),cloudletList(0).getUtilizationOfCpu(),0.01)
  }

  @Test
  def checkVMScheduler():Unit ={
    val vmSchedulerTest  = new VmSchedulerTimeShared()
    val vmScheduler = new DataCenterBase().getVMScheduler("TimeShared")
    Assert.assertNotNull(vmScheduler)
    Assert.assertEquals(vmSchedulerTest.getClass, vmScheduler.getClass())
  }

}
