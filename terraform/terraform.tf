
terraform {
  required_providers {
    opennebula = {
      source = "OpenNebula/opennebula"
      version = "6.8.0"
    }
  }
}

provider "opennebula" {
  endpoint      = "https://nuada.zcu.cz/#vms-tab/3362"
  username      = "rosa"
  password      = "2c5b72dd05b09e9ccc0ece9276a8afb9588d8bcd971c3d9be0299747fab1e669"
}

resource "opennebula_image" "os-image" {
    name = "${var.vm_image_name}"
    datastore_id = "${var.vm_imagedatastore_id}"
    persistent = false
    path = "${var.vm_image_url}"
    permissions = "600"
}

resource "opennebula_virtual_machine" "test-node-vm" {
  # This will create 1 instance:
  count = 1
  name = "test-node-vm"
  description = "First testing VM"
  cpu = 1
  vcpu = 1
  memory = 1024
  permissions = "600"
  group = "users"

  context = {
    NETWORK  = "YES"
    HOSTNAME = "$NAME"
    SSH_PUBLIC_KEY = "${var.vm_ssh_pubkey}"
    START_SCRIPT = "${var.vm_startup_script}"
    #START_SCRIPT_BASE64 = ""
  }
  os {
    arch = "x86_64"
    boot = "disk0"
  }
  disk {
    image_id = opennebula_image.os-image.id
    target   = "vda"
    size     = 12000 # 12GB
  }

  graphics {
    listen = "0.0.0.0"
    type   = "vnc"
  }

  nic {
    network_id = var.vm_network_id
  }
}

#-------OUTPUTS ------------

output "test-node-vm_id" {
  value = "${opennebula_virtual_machine.test-node-vm.*.ip}"
}

#
# EOF
#
